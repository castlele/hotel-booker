package com.castlelecs.booking.dto;

import com.castlelecs.booking.entity.BookingStatus;

import java.time.Instant;
import java.time.LocalDate;

public record BookingResponse(
        Long id,
        Long roomId,
        LocalDate startDate,
        LocalDate endDate,
        BookingStatus status,
        Instant createdAt
) {}

