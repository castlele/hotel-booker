package com.castlelecs.booking.repo;

import com.castlelecs.booking.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByRequestId(String requestId);
    List<Booking> findByUserId(Long userId);
}


