package com.reservation.bookingservice.service;
import com.reservation.bookingservice.model.Route;
import com.reservation.bookingservice.repository.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {


    private final RouteRepository busRouteRepository;

    SearchService(RouteRepository busRouteRepository){
        this.busRouteRepository = busRouteRepository;
    }
    public List<Route> searchBus(String source, String destination) {
        return busRouteRepository.findAllBySourceAndDestination(source,destination);
    }
}
