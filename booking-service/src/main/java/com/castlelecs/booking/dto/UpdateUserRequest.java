package com.castlelecs.booking.dto;

import com.castlelecs.booking.entity.Role;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 3, max = 80) String username,
        @Size(min = 6, max = 100) String password,
        Role role
) {}

