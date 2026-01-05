package com.castlelecs.hotel.service;

import com.castlelecs.hotel.entity.Hotel;
import com.castlelecs.hotel.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final HotelRepository hotelRepository;

    public void saveHotel(Hotel hotel) {
        hotelRepository.save(hotel);
    }

    public Optional<Hotel> getHotelById(Long id) {
        return hotelRepository.findById(id);
    }

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public void deleteHotel(Hotel hotel) {
        hotelRepository.delete(hotel);
    }
}
