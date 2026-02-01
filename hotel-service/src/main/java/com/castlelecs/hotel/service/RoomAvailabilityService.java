package com.castlelecs.hotel.service;

import com.castlelecs.hotel.dto.ConfirmAvailabilityRequest;
import com.castlelecs.hotel.entity.Room;
import com.castlelecs.hotel.entity.RoomLock;
import com.castlelecs.hotel.entity.RoomLockStatus;
import com.castlelecs.hotel.repository.RoomLockRepository;
import com.castlelecs.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RoomAvailabilityService {

    private final RoomRepository roomRepository;
    private final RoomLockRepository lockRepository;

    @Transactional
    public RoomLock confirmAvailability(Long roomId, ConfirmAvailabilityRequest req) {
        if (req.startDate().isAfter(req.endDate()) || req.startDate().isEqual(req.endDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "startDate must be < endDate");
        }

        var existingByRequest = lockRepository.findByRequestId(req.requestId());
        if (existingByRequest.isPresent()) {
            RoomLock lock = existingByRequest.get();
            if (!lock.getRoomId().equals(roomId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "requestId already used for another room");
            }
            return lock;
        }

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        if (room.getAvailable() == null || !room.getAvailable()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is not operationally available");
        }

        boolean overlapped = lockRepository.existsByRoomIdAndStatusAndStartDateLessThanAndEndDateGreaterThan(
                roomId,
                RoomLockStatus.HELD,
                req.endDate(),
                req.startDate()
        );

        if (overlapped) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is already locked for given dates");
        }

        RoomLock lock = RoomLock.builder()
                .roomId(roomId)
                .requestId(req.requestId())
                .bookingId(req.bookingId())
                .startDate(req.startDate())
                .endDate(req.endDate())
                .status(RoomLockStatus.HELD)
                .createdAt(Instant.now())
                .build();

        return lockRepository.save(lock);
    }

    @Transactional
    public RoomLock release(Long roomId, String requestId) {
        if (requestId == null || requestId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "requestId must not be blank");
        }

        RoomLock lock = lockRepository.findByRequestId(requestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lock not found"));

        if (!lock.getRoomId().equals(roomId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "requestId belongs to another room");
        }

        if (lock.getStatus() == RoomLockStatus.RELEASED) {
            return lock;
        }

        lock.setStatus(RoomLockStatus.RELEASED);
        return lockRepository.save(lock);
    }
}

