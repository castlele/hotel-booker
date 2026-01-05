package com.castlelecs.hotel.repository;

import com.castlelecs.hotel.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
