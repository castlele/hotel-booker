package com.castlelecs.hotel.service;

import com.castlelecs.hotel.entity.Room;
import com.castlelecs.hotel.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomService {
    private final RoomRepository roomRepository;

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
}
