package com.castlelecs.hotel.dto;

import com.castlelecs.hotel.entity.RoomLockStatus;

import java.time.Instant;
import java.time.LocalDate;

public record RoomLockResponse(
        Long id,
        Long roomId,
        String requestId,
        String bookingId,
        LocalDate startDate,
        LocalDate endDate,
        RoomLockStatus status,
        Instant createdAt
) {}

