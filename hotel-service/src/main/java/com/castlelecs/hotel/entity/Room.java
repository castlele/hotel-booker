package com.castlelecs.hotel.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@Entity
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    private Hotel hotel;
    private Integer number;
    private Boolean available;
    private Integer timesBooked;
}
