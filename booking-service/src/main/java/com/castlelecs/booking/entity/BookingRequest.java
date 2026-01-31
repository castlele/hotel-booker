package com.castlelecs.booking.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Data
@Builder
public class BookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestId;

    private Long bookingId;

    @Enumerated(EnumType.STRING)
    private BookingRequestStatus status;

    @Builder.Default
    private Instant createdAt = Instant.now();
}

