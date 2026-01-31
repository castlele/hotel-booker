 package com.castlelecs.booking.controller;

import com.castlelecs.booking.dto.BookingResponse;
import com.castlelecs.booking.dto.CreateBookingRequest;
import com.castlelecs.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    // POST /booking — создать бронирование (USER)
    @PostMapping("/booking")
    public ResponseEntity<BookingResponse> createBooking(
            @RequestBody @Valid CreateBookingRequest req,
            Authentication auth
    ) {
        String username = auth.getName();
        return ResponseEntity.ok(bookingService.createBooking(username, req));
    }

    // GET /bookings — история бронирований пользователя (USER)
    @GetMapping("/bookings")
    public ResponseEntity<List<BookingResponse>> history(Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok(bookingService.getHistory(username));
    }

    // GET /booking/{id} — получить бронирование по id (USER)
    @GetMapping("/booking/{id}")
    public ResponseEntity<BookingResponse> getById(@PathVariable Long id, Authentication auth) {
        String username = auth.getName();
        return ResponseEntity.ok(bookingService.getById(username, id));
    }

    // DELETE /booking/{id} — отменить бронирование (USER)
    @DeleteMapping("/booking/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @RequestHeader(name = "X-Request-Id", required = false) String requestId,
            Authentication auth
    ) {
        String username = auth.getName();
        bookingService.cancel(username, id, requestId);
        return ResponseEntity.ok().build();
    }
}

