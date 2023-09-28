package com.reservation.bookingservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservation.bookingservice.exception.Exception;
import com.reservation.bookingservice.exception.FieldException;
import com.reservation.bookingservice.exception.BusResourceNotFoundException;
import com.reservation.bookingservice.constants.BookingStatus;
import com.reservation.bookingservice.dto.BookingReqDTO;
import com.reservation.bookingservice.dto.BookingResDTO;
import com.reservation.bookingservice.dto.InventoryDTO;
import com.reservation.bookingservice.messages.BookingMessage;
import com.reservation.bookingservice.messages.BusBookingMessage;
import com.reservation.bookingservice.messages.MessageBroker;
import com.reservation.bookingservice.messages.MessageDestinationConst;
import com.reservation.bookingservice.model.Booking;
import com.reservation.bookingservice.model.Passenger;
import com.reservation.bookingservice.model.Route;
import com.reservation.bookingservice.repository.BookingRepository;
import com.reservation.bookingservice.repository.RouteRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BookingService {

    private BookingRepository bookingRepository;

    private final WebClient.Builder webClientBuilder;

    private final RouteRepository routeRepository;

    private final MessageBroker messageBroker;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    private static final String INVENTORY_SERVICE = "inventoryService";


    @Value("${endpoint.inventory.host}")
    private String INVENTORY_HOST;
    @Value("${endpoint.inventory.getInventories}")
    private String GET_INVENTORIES;

    public BookingService(BookingRepository bookingRepository, WebClient.Builder webClientBuilder, RouteRepository routeRepository, MessageBroker messageBroker, ObjectMapper objectMapper, RestTemplate restTemplate) {
        this.bookingRepository = bookingRepository;
        this.webClientBuilder = webClientBuilder;
        this.routeRepository = routeRepository;
        this.messageBroker = messageBroker;
        this.objectMapper = objectMapper;
        this.restTemplate = restTemplate;
    }


    @CircuitBreaker(name = INVENTORY_SERVICE, fallbackMethod = "fetchDefaultBusInventory")
//    @Retry(name = INVENTORY_SERVICE, fallbackMethod = "fetchDefaultBusInventory")
    public Optional<InventoryDTO> fetchBusInventoryOld(Integer busId) {
        System.out.println("API call fetchBusInventory");
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<InventoryDTO> entity = new HttpEntity<>(headers);
        ResponseEntity<InventoryDTO> res = restTemplate.exchange(INVENTORY_HOST + GET_INVENTORIES,
                HttpMethod.GET, entity, InventoryDTO.class, busId);
        if (res.getStatusCode() == HttpStatus.OK && res.hasBody()) {
            return Optional.ofNullable(res.getBody());
        } else {
            return Optional.empty();
        }
    }


    public Optional<InventoryDTO> fetchDefaultBusInventory(Exception e) {
        return Optional.empty();
    }
    public BookingResDTO book(BookingReqDTO bookingReDto) {

        Optional<InventoryDTO> busInventoryDto = fetchBusInventory(bookingReDto.getBusId());

        if (busInventoryDto.isPresent()) {
            InventoryDTO busInventoryDetail = busInventoryDto.get();
            Route busRoute = routeRepository.findById(bookingReDto.getBusId()).orElse(null);
            if (busRoute == null) {
                throw new Exception("BusRoute detail not found");
            }
            List<Passenger> passengers = bookingReDto.getPassengerDetails();

            if (busInventoryDetail.getAvailableSeats() >= passengers.size()) {
                Booking booking = new Booking();
                booking.setBusId(bookingReDto.getBusId());
                booking.setCustomerId(bookingReDto.getCustomerId());
                booking.setBookingDate(bookingReDto.getBookingDate());
                booking.setNoOfSeats(passengers.size());
                booking.setBookingStatus(BookingStatus.PENDING);
                booking.setTotalAmount(busRoute.getFareAmount() * booking.getNoOfSeats());
                passengers.forEach(passenger -> passenger.setBooking(booking));
                bookingReDto.setPassengerDetails(passengers);
                booking.setPassengers(passengers);
                Booking newBooking = bookingRepository.saveAndFlush(booking);
                messageBroker.sendBookingMessage(MessageDestinationConst.DEST_PROCESS_PAYMENT, new BookingMessage(newBooking.getId(), newBooking.getTotalAmount()));
                return new BookingResDTO(BookingStatus.PENDING, newBooking.getId(), newBooking.getTotalAmount());
            } else {
                throw new FieldException("Insufficient seats");
            }
        } else {
            throw new Exception("Could not retrieve bus inventory");
        }
    }



    public List<Booking> getBookings(Integer customerId) {
        return bookingRepository.findAllByCustomerId(customerId);
    }

    public Optional<InventoryDTO> fetchBusInventory(Integer busId) {
        return webClientBuilder
                .baseUrl(INVENTORY_HOST)
                .build()
                .get()
                .uri(GET_INVENTORIES, busId)
                .retrieve()
                .bodyToMono(InventoryDTO.class).blockOptional();
    }

    public Optional<Booking> getBooking(Integer bookingId) {
        return bookingRepository.findById(bookingId);
    }

    public void cancelBooking(Integer bookingId) {
        Optional<Booking> bookingDetail = bookingRepository.findByIdAndBookingStatus(bookingId, BookingStatus.CONFIRMED);
        if (bookingDetail.isPresent()) {
            Booking booking = bookingDetail.get();
            booking.setBookingStatus(BookingStatus.CANCEL);
            bookingRepository.saveAndFlush(booking);
//            messageBroker.sendBookingMessage(MessageDestinationConst.DEST_INITIATE_PAYMENT_REFUND, new BookingMessage(bookingId, null));
        } else {
            throw new BusResourceNotFoundException(String.format("No Confirm Booking details with id %d found", bookingId));
        }
    }

    @JmsListener(destination = MessageDestinationConst.DEST_UPDATE_BOOKING)
    public void receiveMessage(Map<String, Object> object) {
        final BusBookingMessage busBookingMessage = objectMapper.convertValue(object, BusBookingMessage.class);
        System.out.println("Received message: " + busBookingMessage);
        Booking bookingDetail = bookingRepository.findById(busBookingMessage.getBookingId()).orElse(null);
        bookingDetail.setBookingStatus(BookingStatus.CONFIRMED);
        bookingRepository.saveAndFlush(bookingDetail);
    }

}
