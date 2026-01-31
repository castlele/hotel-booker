package com.castlelecs.booking.repository;

import com.castlelecs.booking.entity.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    Optional<BookingRequest> findByRequestId(String requestId);
}

