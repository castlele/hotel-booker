package com.castlelecs.booking.web;

import com.castlelecs.booking.model.Booking;
import com.castlelecs.booking.repo.BookingRepository;
import com.castlelecs.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookings")
@io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "bearer-jwt")
public class BookingController {
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    public BookingController(BookingService bookingService, BookingRepository bookingRepository) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
    }

    @PostMapping
    public Booking create(@AuthenticationPrincipal Jwt jwt, @RequestBody Map<String, String> req) {
        Long userId = Long.parseLong(jwt.getSubject());
        Long roomId = Long.valueOf(req.get("roomId"));
        LocalDate start = LocalDate.parse(req.get("startDate"));
        LocalDate end = LocalDate.parse(req.get("endDate"));
        String requestId = req.get("requestId");
        return bookingService.createBooking(userId, roomId, start, end, requestId);
    }

    @GetMapping
    public List<Booking> myBookings(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        return bookingRepository.findByUserId(userId);
    }

    @GetMapping("/suggestions")
    public reactor.core.publisher.Mono<java.util.List<BookingService.RoomView>> suggestions() {
        return bookingService.getRoomSuggestions();
    }

    @GetMapping("/all")
    public ResponseEntity<List<Booking>> all(@AuthenticationPrincipal Jwt jwt) {
        String scope = jwt.getClaimAsString("scope");
        if ("ADMIN".equals(scope)) {
            return ResponseEntity.ok(bookingRepository.findAll());
        }
        return ResponseEntity.status(403).build();
    }
}


