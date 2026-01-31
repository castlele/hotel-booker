package com.castlelecs.hotel.service;

import com.castlelecs.hotel.dto.CreateRoomRequest;
import com.castlelecs.hotel.entity.Hotel;
import com.castlelecs.hotel.entity.Room;
import com.castlelecs.hotel.repository.HotelRepository;
import com.castlelecs.hotel.repository.RoomRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;

    public Room saveRoom(Room room) {
        return roomRepository.save(room);
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public void deleteRoom(Room room) {
        roomRepository.delete(room);
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailableTrue();
    }

    public List<Room> getSuggestedRooms() {
        return roomRepository
                .findByAvailableTrue()
                .stream()
                .sorted(Comparator.comparing(Room::getTimesBooked))
                .toList();
    }

    @Transactional
    public Room createRoom(CreateRoomRequest req) {
        Hotel hotel = hotelRepository.findById(req.hotelId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        Room room = Room.builder()
        .hotel(hotel)
        .number(req.number())
        .available(req.available() != null ? req.available() : true)
        .timesBooked(0)
        .build();

        return roomRepository.save(room);
    }
}
