 package com.castlelecs.booking.service;

import com.castlelecs.booking.entity.User;
import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    public record AuthToken(String token, long expiresInSeconds) {}

    public AuthToken issueToken(User user) {
        Instant now = Instant.now();
        long expiresIn = 3600;

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("booking-service")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresIn))
                .subject(user.getUsername())
                .claim("role", user.getRole().name()) // важно: "ADMIN"/"USER"
                .build();

        // ЯВНО HS256 (чтобы не пытался подобрать другое)
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthToken(token, expiresIn);
    }
}

