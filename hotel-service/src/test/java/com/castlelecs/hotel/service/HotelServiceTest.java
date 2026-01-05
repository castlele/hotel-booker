package com.castlelecs.hotel.service;

import com.castlelecs.hotel.entity.Hotel;
import com.castlelecs.hotel.repository.HotelRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class HotelServiceTest {
    @Autowired
    private HotelService sut;

    @Autowired
    private HotelRepository hotelRepository;

    // CRUD

    @Test
    void createNewHotel() {
        Hotel hotel = Hotel.builder()
                .name(DEFAULT_HOTEL_NAME)
                .address(DEFAULT_HOTEL_ADDRESS)
                .build();

        sut.saveHotel(hotel);

        assertEquals(DEFAULT_HOTEL_NAME, hotelRepository.getReferenceById(hotel.getId()).getName());
    }

    @Test
    void getHotelById() {
        Hotel hotel = Hotel.builder()
                .name(DEFAULT_HOTEL_NAME)
                .address(DEFAULT_HOTEL_ADDRESS)
                .build();
        sut.saveHotel(hotel);

        Optional<Hotel> actualHotel = sut.getHotelById(hotel.getId());

        assertTrue(actualHotel.isPresent());
        assertEquals(hotel, actualHotel.get());
    }

    @Test
    void getAllAvailableHotels() {
        List<Hotel> expectedHotels = Arrays.asList(
                Hotel.builder()
                        .name(DEFAULT_HOTEL_NAME + "1")
                        .address(DEFAULT_HOTEL_ADDRESS)
                        .build(),
                Hotel.builder()
                        .name(DEFAULT_HOTEL_NAME + "2")
                        .address(DEFAULT_HOTEL_ADDRESS)
                        .build()
        );
        for (Hotel hotel : expectedHotels) {
            sut.saveHotel(hotel);
        }

        List<Hotel> hotels = sut.getAllHotels();

        for (Hotel expected : expectedHotels) {
            Optional<Hotel> hotel = hotels.stream().filter(h -> Objects.equals(h.getId(), expected.getId())).findFirst();

            assertNotNull(hotel);
        }
        assertEquals(expectedHotels.size(), hotels.size());
    }

    @Test
    void updateExistingHotel() {
        String expectedAddress = "Another Address";
        Hotel expectedHotel = saveHotel();
        expectedHotel.setAddress(expectedAddress);

        sut.saveHotel(expectedHotel);
        Optional<Hotel> hotel = sut.getHotelById(expectedHotel.getId());

        assertTrue(hotel.isPresent());
        assertEquals(expectedHotel.getId(), hotel.get().getId());
        assertEquals(expectedAddress, hotel.get().getAddress());
    }

    @Test
    void deleteHotel() {
        Hotel hotel = saveHotel();

        sut.deleteHotel(hotel);

        assertEquals(0, sut.getAllHotels().size());
    }

    @Test
    void deleteHotelThatDoesNotExist() {
        Hotel hotel = saveHotel();
        Hotel hotelToDelete = Hotel
                .builder()
                .name("bar")
                .address("Random address")
                .build();

        sut.deleteHotel(hotelToDelete);

        assertEquals(1, sut.getAllHotels().size());
        assertEquals(hotel, sut.getHotelById(hotel.getId()).get());
    }

    private Hotel saveHotel() {
        Hotel hotel = Hotel.builder()
                .name(DEFAULT_HOTEL_NAME)
                .address(DEFAULT_HOTEL_ADDRESS)
                .build();

        hotelRepository.save(hotel);

        return hotel;
    }

    static private final String DEFAULT_HOTEL_NAME = "foo";
    static private final String DEFAULT_HOTEL_ADDRESS = "Address";
}
