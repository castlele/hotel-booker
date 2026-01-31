package com.castlelecs.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.castlelecs.hotel.entity.RoomReservation;

public interface RoomReservationRepository extends JpaRepository<RoomReservation, Long> {
}
