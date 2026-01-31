package com.castlelecs.booking.service;

import java.time.LocalDate;
import java.util.List;

import com.castlelecs.booking.dto.RoomBriefDto;

public interface HotelClient {
    List<RoomBriefDto> recommendRooms(LocalDate startDate, LocalDate endDate);
    void releaseRoom(Long roomId, String bookingId, String requestId);
}

