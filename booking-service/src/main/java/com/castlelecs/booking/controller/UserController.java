package com.castlelecs.booking.controller;

import com.castlelecs.booking.dto.AuthRequest;
import com.castlelecs.booking.dto.AuthResponse;
import com.castlelecs.booking.dto.RegisterRequest;
import com.castlelecs.booking.entity.User;
import com.castlelecs.booking.service.JwtService;
import com.castlelecs.booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest req) {
        User user = userService.register(req);
        JwtService.AuthToken token = jwtService.issueToken(user);

        return ResponseEntity.ok(new AuthResponse(token.token(), token.expiresInSeconds(), "Bearer"));
    }

    @PostMapping("/auth")
    public ResponseEntity<AuthResponse> auth(@RequestBody @Valid AuthRequest req) {
        User user = userService.authenticate(req.username(), req.password());
        JwtService.AuthToken token = jwtService.issueToken(user);

        return ResponseEntity.ok(
                new AuthResponse(token.token(), token.expiresInSeconds(), "Bearer")
        );
    }
}
