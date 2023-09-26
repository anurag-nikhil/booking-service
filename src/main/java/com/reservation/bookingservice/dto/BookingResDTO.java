package com.reservation.bookingservice.dto;

import com.reservation.bookingservice.constants.BookingStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingResDTO {

    private BookingStatus bookingStatus;
    private Integer bookingId;
    private Float bookingAmount;
}
