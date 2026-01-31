package com.castlelecs.booking.service;

import com.castlelecs.booking.dto.BookingResponse;
import com.castlelecs.booking.dto.CreateBookingRequest;
import com.castlelecs.booking.dto.RoomBriefDto;
import com.castlelecs.booking.entity.*;
import com.castlelecs.booking.repository.BookingRepository;
import com.castlelecs.booking.repository.BookingRequestRepository;
import com.castlelecs.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final UserRepository userRepository;
    private final HotelClient hotelClient;

    @Transactional
    public BookingResponse createBooking(String username, CreateBookingRequest req) {
        validateDates(req.startDate(), req.endDate());

        // 1) идемпотентность по requestId
        var existingReq = bookingRequestRepository.findByRequestId(req.requestId());
        if (existingReq.isPresent() && existingReq.get().getBookingId() != null) {
            Booking existing = bookingRepository.findById(existingReq.get().getBookingId())
                    .orElseThrow(() -> new IllegalStateException("Idempotency record exists but booking not found"));
            ensureOwner(existing, username);
            return toResponse(existing);
        }

        // 2) зафиксировать STARTED (ловим гонки уникальным индексом)
        BookingRequest started;
        try {
            started = bookingRequestRepository.save(BookingRequest.builder()
                    .requestId(req.requestId())
                    .status(BookingRequestStatus.STARTED)
                    .build());
        } catch (DataIntegrityViolationException e) {
            BookingRequest br = bookingRequestRepository.findByRequestId(req.requestId())
                    .orElseThrow();
            if (br.getBookingId() == null) {
                throw ApiException.conflict("Request is being processed");
            }
            Booking existing = bookingRepository.findById(br.getBookingId()).orElseThrow();
            ensureOwner(existing, username);
            return toResponse(existing);
        }

        // 3) определить roomId
        Long selectedRoomId;
        if (req.autoSelect()) {
            List<RoomBriefDto> rooms = hotelClient.recommendRooms(req.startDate(), req.endDate());
            if (rooms == null || rooms.isEmpty()) {
                started.setStatus(BookingRequestStatus.FAILED);
                bookingRequestRepository.save(started);
                throw ApiException.conflict("No available rooms for selected dates");
            }
            // список по ТЗ уже отсортирован times_booked ASC, id ASC, но на всякий случай:
            selectedRoomId = rooms.stream()
                    .min(Comparator.<RoomBriefDto>comparingLong(RoomBriefDto::timesBooked).thenComparing(RoomBriefDto::id))
                    .get()
                    .id();
        } else {
            if (req.roomId() == null) {
                started.setStatus(BookingRequestStatus.FAILED);
                bookingRequestRepository.save(started);
                throw ApiException.badRequest("roomId is required when autoSelect=false");
            }
            selectedRoomId = req.roomId();
        }

        // 4) локальная транзакция: создать PENDING
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));

        Booking booking = bookingRepository.save(Booking.builder()
                .user(user)
                .roomId(selectedRoomId)
                .startDate(req.startDate())
                .endDate(req.endDate())
                .status(BookingStatus.PENDING)
                .createdAt(Instant.now())
                .build());

        started.setBookingId(booking.getId());

        // TODO: Здесь будет двухшаговая согласованность с Hotel Service:
        //  - вызвать confirm-availability (timeout+retries)
        //  - если OK -> CONFIRMED
        //  - если ошибка/timeout -> CANCELLED + release
        //
        // Пока фиксируем как CONFIRMED для демонстрации базового API (можешь заменить на PENDING если хочешь).
        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);

        started.setStatus(BookingRequestStatus.COMPLETED);
        bookingRequestRepository.save(started);

        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getHistory(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> ApiException.unauthorized("User not found"));

        return bookingRepository.findAllByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(String username, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ApiException.notFound("Booking not found"));
        ensureOwner(booking, username);
        return toResponse(booking);
    }

    @Transactional
    public void cancel(String username, Long bookingId, String requestId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> ApiException.notFound("Booking not found"));
        ensureOwner(booking, username);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return; // идемпотентно
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);

        // Компенсация (если ты реально ставишь lock в hotel-service)
        // Сейчас вызов безопасен, но может падать если hotel-service не поднят.
        // Поэтому лучше включить позже, когда реализуешь confirm/release.
        if (requestId != null && !requestId.isBlank()) {
            try {
                hotelClient.releaseRoom(booking.getRoomId(), booking.getId().toString(), requestId);
            } catch (Exception ignored) {
                // TODO: логировать; можно добавить retry/backoff позже
            }
        }
    }

    private void ensureOwner(Booking booking, String username) {
        if (!booking.getUser().getUsername().equals(username)) {
            throw ApiException.forbidden("Access denied");
        }
    }

    private void validateDates(java.time.LocalDate start, java.time.LocalDate end) {
        if (end.isBefore(start) || end.isEqual(start)) {
            throw ApiException.badRequest("endDate must be after startDate");
        }
    }

    private BookingResponse toResponse(Booking b) {
        return new BookingResponse(
                b.getId(),
                b.getRoomId(),
                b.getStartDate(),
                b.getEndDate(),
                b.getStatus(),
                b.getCreatedAt()
        );
    }
}

