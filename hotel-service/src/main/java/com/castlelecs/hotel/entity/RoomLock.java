package com.castlelecs.hotel.entity;

import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class RoomLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // we store roomId explicitly to avoid LAZY serialization issues
    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "request_id", nullable = false, unique = true, length = 120)
    private String requestId;

    // optional: to correlate with bookingId (nice for logs)
    @Column(name = "booking_id", length = 120)
    private String bookingId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoomLockStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}

