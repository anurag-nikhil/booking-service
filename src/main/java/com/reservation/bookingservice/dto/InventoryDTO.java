package com.reservation.bookingservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class InventoryDTO {
    private Integer id;
    private Integer busId;
    private Integer availableSeats;
}
