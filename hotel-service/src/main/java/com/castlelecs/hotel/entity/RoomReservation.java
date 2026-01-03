package com.castlelecs.hotel.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Builder
@Data
@Entity
public class RoomReservation {
    public enum ReservationStatus { ON_HOLD, CANCELED, CONFIRMED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Room room;
    private String bookingId;
    private String requestId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant expiresAt;
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;
}
