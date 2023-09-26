package com.reservation.bookingservice.repository;

import com.reservation.bookingservice.constants.BookingStatus;
import com.reservation.bookingservice.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, String> {

    Optional<Booking> findByIdAndBookingStatus(Integer id, BookingStatus bookingStatus);

    Optional<Booking> findById(Integer bookingId);


    List<Booking> findAllByBookingStatus(BookingStatus bookingStatus);

    List<Booking> findAllByCustomerId(Integer customerId);
}