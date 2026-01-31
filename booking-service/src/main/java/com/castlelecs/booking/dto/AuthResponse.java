package com.castlelecs.booking.dto;

public record AuthResponse(
        String token,
        long expiresInSeconds,
        String tokenType
) {}
