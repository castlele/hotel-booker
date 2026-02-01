package com.castlelecs.hotel.controller;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.castlelecs.hotel.dto.ConfirmAvailabilityRequest;
import com.castlelecs.hotel.dto.CreateRoomRequest;
import com.castlelecs.hotel.dto.RoomLockResponse;
import com.castlelecs.hotel.dto.RoomResponse;
import com.castlelecs.hotel.entity.Room;
import com.castlelecs.hotel.entity.RoomLock;
import com.castlelecs.hotel.service.RoomAvailabilityService;
import com.castlelecs.hotel.service.RoomService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
class RoomController {

    private final RoomService roomService;
    private final RoomAvailabilityService availabilityService;


    @GetMapping
    public List<Room> getRooms() {
        return roomService.getAvailableRooms();
    }

    @GetMapping("/recommended")
    public List<Room> getRecommendedRooms() {
        return roomService.getSuggestedRooms();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Room> getRoomById(Long id) {
        return getRoomByIdOrNotFound(id, ResponseEntity::ok);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@RequestBody @Valid CreateRoomRequest req) {
        Room room = roomService.createRoom(req);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                new RoomResponse(
                        room.getId(),
                        room.getHotel().getId(),
                        room.getNumber(),
                        room.getAvailable(),
                        room.getTimesBooked()
                )
        );
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Room> updateRoom(@PathVariable Long id, @RequestBody Room newRoom) {
        return getRoomByIdOrNotFound(id, room -> {
            newRoom.setId(id);

            return ResponseEntity.ok(roomService.saveRoom(newRoom));
        });
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        Optional<Room> room = roomService.getRoomById(id);

        if (room.isPresent()) {
            roomService.deleteRoom(room.get());
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/confirm-availability")
    @ResponseStatus(HttpStatus.OK)
    public RoomLockResponse confirmAvailability(@PathVariable("id") Long roomId,
        @RequestBody @Valid ConfirmAvailabilityRequest req) {
        RoomLock lock = availabilityService.confirmAvailability(roomId, req);
        return toResponse(lock);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/release")
    @ResponseStatus(HttpStatus.OK)
    public RoomLockResponse release(@PathVariable("id") Long roomId,
        @RequestParam("requestId") String requestId) {
        RoomLock lock = availabilityService.release(roomId, requestId);
        return toResponse(lock);
    }

    private static RoomLockResponse toResponse(RoomLock lock) {
        return new RoomLockResponse(
            lock.getId(),
            lock.getRoomId(),
            lock.getRequestId(),
            lock.getBookingId(),
            lock.getStartDate(),
            lock.getEndDate(),
            lock.getStatus(),
            lock.getCreatedAt()
        );
    }

    private ResponseEntity<Room> getRoomByIdOrNotFound(Long id, Function<Room, ResponseEntity<Room>> mapping) {
        return roomService.getRoomById(id)
        .map(mapping)
        .orElse(ResponseEntity.notFound().build());
    }
}
