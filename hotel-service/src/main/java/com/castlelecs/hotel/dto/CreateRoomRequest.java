package com.castlelecs.hotel.dto;

public record CreateRoomRequest(Long hotelId, Integer number, Boolean available) {}

