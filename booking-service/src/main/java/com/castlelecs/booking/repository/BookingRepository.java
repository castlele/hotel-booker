package com.castlelecs.booking.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.castlelecs.booking.entity.Booking;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
