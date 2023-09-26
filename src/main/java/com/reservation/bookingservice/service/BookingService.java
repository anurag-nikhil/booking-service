package com.reservation.bookingservice.service;

import com.reservation.bookingservice.exception.Exception;
import com.reservation.bookingservice.exception.FieldException;
import com.reservation.bookingservice.exception.BusResourceNotFoundException;
import com.reservation.bookingservice.constants.BookingStatus;
import com.reservation.bookingservice.dto.BookingReqDTO;
import com.reservation.bookingservice.dto.BookingResDTO;
import com.reservation.bookingservice.dto.InventoryDTO;
import com.reservation.bookingservice.model.Booking;
import com.reservation.bookingservice.model.Passenger;
import com.reservation.bookingservice.model.Route;
import com.reservation.bookingservice.repository.BookingRepository;
import com.reservation.bookingservice.repository.RouteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Service
public class BookingService {

    private BookingRepository bookingRepository;

    private final WebClient.Builder webClientBuilder;

    private final RouteRepository routeRepository;
  //  String bookingid ="BOOK"+(int)(Math.random()*100000);

    @Value("${endpoint.inventory.host}")
    private String INVENTORY_HOST;
    @Value("${endpoint.inventory.getInventories}")
    private String GET_INVENTORIES;

    public BookingService(BookingRepository bookingRepository, WebClient.Builder webClientBuilder, RouteRepository routeRepository) {
        this.bookingRepository = bookingRepository;
        this.webClientBuilder = webClientBuilder;
        this.routeRepository = routeRepository;
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
             //   messageBroker.sendBookingMessage(MessageDestinationConst.DEST_PROCESS_PAYMENT, new BookingMessage(newBooking.getId(), newBooking.getTotalAmount()));
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
        System.out.println("API call fetchBusInventory");
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
}
