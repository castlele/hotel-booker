package com.castlelecs.hotel.dto;

public record RoomResponse(Long id, Long hotelId, Integer number, Boolean available, Integer timesBooked) {}

