package com.castlelecs.hotel.controller;

import com.castlelecs.hotel.entity.Hotel;
import com.castlelecs.hotel.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @GetMapping
    public List<Hotel> getHotels() {
        return hotelService.getAllHotels();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Hotel> getHotelById(@PathVariable Long id) {
        return getHotelByIdOrNotFound(id, ResponseEntity::ok);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public Hotel createHotel(@RequestBody Hotel hotel) {
        return hotelService.saveHotel(hotel);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<Hotel> updateHotel(@PathVariable Long id, @RequestBody Hotel newHotel) {
        return getHotelByIdOrNotFound(id, hotel -> {
            newHotel.setId(id);

            return ResponseEntity.ok(hotelService.saveHotel(newHotel));
        });
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable Long id) {
        Optional<Hotel> hotel = hotelService.getHotelById(id);

        if (hotel.isPresent()) {
            hotelService.deleteHotel(hotel.get());
            return ResponseEntity.ok().build();
        }

        return ResponseEntity.noContent().build();
    }

    private ResponseEntity<Hotel> getHotelByIdOrNotFound(Long id, Function<Hotel, ResponseEntity<Hotel>> mapping) {
        return hotelService.getHotelById(id)
                .map(mapping)
                .orElse(ResponseEntity.notFound().build());
    }
}
