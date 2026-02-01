package com.castlelecs.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ConfirmAvailabilityRequest(
        @NotBlank String requestId,
        String bookingId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {}

