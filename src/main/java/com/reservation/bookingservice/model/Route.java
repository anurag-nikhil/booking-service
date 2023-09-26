package com.reservation.bookingservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

import javax.persistence.*;


@Getter
@Setter
@Entity
@Table(name = "route")
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "busid", nullable = false)
    private Integer id;

    @Column(name = "busnumber", length = 20)
    private String busNumber;

    @Column(name = "bustype", length = 20)
    private String busType;

    @Column(name = "totalseats")
    private Integer totalSeats;

    @Column(name = "source", nullable = false, length = 100)
    private String source;

    @Column(name = "destination", nullable = false, length = 100)
    private String destination;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "fare_amount")
    private Float fareAmount;
}
