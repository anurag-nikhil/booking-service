package com.reservation.bookingservice.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingMessage {
    private Integer bookingId;
    private Float bookingAmount;
}
