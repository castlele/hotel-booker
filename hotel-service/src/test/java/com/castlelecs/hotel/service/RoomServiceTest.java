package com.castlelecs.hotel.service;

import com.castlelecs.hotel.entity.Room;
import com.castlelecs.hotel.repository.RoomRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class RoomServiceTest {
    @Autowired
    private RoomService sut;

    @Autowired
    private RoomRepository roomRepository;

    // CRUD

    @Test
    void createNewRoom() {
        Room room = Room.builder()
                .number(DEFAULT_ROOM_NUMBER)
                .available(true)
                .timesBooked(0)
                .build();

        sut.saveRoom(room);

        assertEquals(DEFAULT_ROOM_NUMBER, roomRepository.getReferenceById(room.getId()).getNumber());
    }

    @Test
    void getRoomById() {
        Room room = Room.builder()
                .number(DEFAULT_ROOM_NUMBER)
                .available(true)
                .timesBooked(0)
                .build();
        sut.saveRoom(room);

        Optional<Room> actualRoom = sut.getRoomById(room.getId());

        assertTrue(actualRoom.isPresent());
        assertEquals(room, actualRoom.get());
    }

    @Test
    void getAllRooms() {
        List<Room> expectedRooms = Arrays.asList(
                Room.builder()
                        .number(DEFAULT_ROOM_NUMBER)
                        .available(true)
                        .timesBooked(0)
                        .build(),
                Room.builder()
                        .number(DEFAULT_ROOM_NUMBER + 1)
                        .available(true)
                        .timesBooked(0)
                        .build()
        );
        for (Room room : expectedRooms) {
            sut.saveRoom(room);
        }

        List<Room> rooms = sut.getAllRooms();

        for (Room expected : expectedRooms) {
            Optional<Room> room = rooms.stream().filter(h -> Objects.equals(h.getId(), expected.getId())).findFirst();

            assertNotNull(room);
        }
        assertEquals(expectedRooms.size(), rooms.size());
    }

    @Test
    void updateExistingRoom() {
        Room expectedRoom = saveRoom();
        expectedRoom.setAvailable(false);

        sut.saveRoom(expectedRoom);
        Optional<Room> room = sut.getRoomById(expectedRoom.getId());

        assertTrue(room.isPresent());
        assertEquals(expectedRoom.getId(), room.get().getId());
        assertFalse(room.get().getAvailable());
    }

    @Test
    void deleteRoom() {
        Room room = saveRoom();

        sut.deleteRoom(room);

        assertEquals(0, sut.getAllRooms().size());
    }

    @Test
    void deleteRoomThatDoesNotExist() {
        Room room = saveRoom();
        Room roomToDelete = Room
                .builder()
                .number(DEFAULT_ROOM_NUMBER)
                .available(true)
                .timesBooked(0)
                .build();

        sut.deleteRoom(roomToDelete);

        assertEquals(1, sut.getAllRooms().size());
        assertEquals(room, sut.getRoomById(room.getId()).get());
    }

    // Available rooms

    @Test
    void getAllAvailableRooms() {
        Integer roomsAmount = 10;
        List<Room> allRooms = saveMultipleRooms(roomsAmount);
        Room unavailableRoom = allRooms.stream().findFirst().orElseThrow();
        unavailableRoom.setAvailable(false);
        roomRepository.save(unavailableRoom);

        List<Room> availableRooms = sut.getAvailableRooms();

        assertNotEquals(allRooms.size(), availableRooms.size());
        assertTrue(availableRooms.stream().allMatch(Room::getAvailable));
    }

    // Recommended rooms

    @Test
    void getAllRecommentedRooms() {
        int roomsAmount = 10;
        List<Room> allRooms = saveMultipleRooms(roomsAmount);

        for (int i = 0; i < roomsAmount; i++) {
            Room room = allRooms.get(i);

            if (i % 2 == 0) {
                room.setAvailable(false);
            }

            room.setTimesBooked(i);
        }

        List<Room> availableRooms = sut.getSuggestedRooms();

        assertNotEquals(allRooms.size(), availableRooms.size());
        assertEquals(roomsAmount/2, availableRooms.size());
        assertTrue(availableRooms.stream().allMatch(Room::getAvailable));
        assertEquals(allRooms.stream().filter(Room::getAvailable).sorted(Comparator.comparing(Room::getTimesBooked)).toList(), availableRooms);
    }

    private List<Room> saveMultipleRooms(Integer size) {
        List<Room> rooms = IntStream.range(0, size).mapToObj(index ->
                Room
                        .builder()
                        .number(index)
                        .timesBooked(0)
                        .available(true)
                        .build()
        ).toList();

        for (Room room : rooms) {
            roomRepository.save(room);
        }

        return rooms;
    }

    private Room saveRoom() {
        Room room = Room.builder()
                .number(DEFAULT_ROOM_NUMBER)
                .available(true)
                .timesBooked(0)
                .build();

        roomRepository.save(room);

        return room;
    }

    static private final Integer DEFAULT_ROOM_NUMBER = 228;
}
