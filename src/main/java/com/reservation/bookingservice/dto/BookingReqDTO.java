package com.reservation.bookingservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.List;
import com.reservation.bookingservice.model.Passenger;

@Getter
@Setter
public class BookingReqDTO {

    private Integer busId;
    private Integer customerId;
    private List<Passenger> passengerDetails;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime bookingDate;
}
