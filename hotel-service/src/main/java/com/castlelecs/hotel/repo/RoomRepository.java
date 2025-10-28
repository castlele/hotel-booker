package com.castlelecs.hotel.repo;

import com.castlelecs.hotel.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}


