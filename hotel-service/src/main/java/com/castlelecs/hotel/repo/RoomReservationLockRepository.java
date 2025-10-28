package com.castlelecs.hotel.repo;

import com.castlelecs.hotel.model.RoomReservationLock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomReservationLockRepository extends JpaRepository<RoomReservationLock, Long> {
    Optional<RoomReservationLock> findByRequestId(String requestId);
    List<RoomReservationLock> findByRoomIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long roomId,
            List<RoomReservationLock.Status> statuses,
            LocalDate endInclusive,
            LocalDate startInclusive
    );
}


