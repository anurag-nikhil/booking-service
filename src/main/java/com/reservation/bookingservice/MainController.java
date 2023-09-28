package com.reservation.bookingservice;


import com.reservation.bookingservice.dto.BookingReqDTO;
import com.reservation.bookingservice.dto.BookingResDTO;
import com.reservation.bookingservice.exception.BusResourceNotFoundException;
import com.reservation.bookingservice.model.Booking;
import com.reservation.bookingservice.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1")
public class MainController {


 private final BookingService bookingService;

 public MainController(BookingService bookingService) {
  this.bookingService = bookingService;
 }


 @PostMapping("/booking")
 public ResponseEntity<BookingResDTO>  book(@RequestBody BookingReqDTO bookingRequestDto) {
  return ResponseEntity.ok().body(bookingService.book(bookingRequestDto));
 }

 @GetMapping("/booking/{booking_id}")
 public ResponseEntity<Booking> getBooking(@PathVariable("booking_id") Integer bookingId) {
  Optional<Booking> booking = bookingService.getBooking(bookingId);

  if (booking.isPresent()) {
   return ResponseEntity.status(HttpStatus.OK).body(booking.get());
  } else {
   throw new BusResourceNotFoundException(String.format("Booking details with id %d not found", bookingId));
  }
 }

 @GetMapping("/booking/customers/{customer_id}")
 public ResponseEntity<List<Booking>> getBookings(@PathVariable("customer_id") Integer customerId) {
  List<Booking> bookings = bookingService.getBookings(customerId);
  return ResponseEntity.status(HttpStatus.OK).body(bookings);
 }

 @DeleteMapping("/booking/{booking_id}")
 public ResponseEntity cancelBooking(@PathVariable("booking_id") Integer bookingId){
  bookingService.cancelBooking(bookingId);
  return ResponseEntity.status(HttpStatus.OK).build();
 }

}
