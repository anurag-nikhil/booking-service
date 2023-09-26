package com.reservation.bookingservice.repository;

import com.reservation.bookingservice.model.Route;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Integer> {

    List<Route> findAllBySourceAndDestination(String source, String destination);
}