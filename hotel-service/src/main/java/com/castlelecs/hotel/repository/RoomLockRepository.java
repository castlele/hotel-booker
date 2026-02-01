package com.castlelecs.hotel.repository;

import com.castlelecs.hotel.entity.RoomLock;
import com.castlelecs.hotel.entity.RoomLockStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface RoomLockRepository extends JpaRepository<RoomLock, Long> {

    Optional<RoomLock> findByRequestId(String requestId);

    boolean existsByRoomIdAndStatusAndStartDateLessThanAndEndDateGreaterThan(
        Long roomId,
        RoomLockStatus status,
        LocalDate endDateExclusive,
        LocalDate startDateInclusive
    );
}

