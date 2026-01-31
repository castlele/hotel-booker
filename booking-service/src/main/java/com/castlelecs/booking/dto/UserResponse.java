package com.castlelecs.booking.dto;

import com.castlelecs.booking.entity.Role;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        Role role,
        Instant createdAt
) {}

