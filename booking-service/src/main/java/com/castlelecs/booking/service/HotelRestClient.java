package com.castlelecs.booking.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.castlelecs.booking.dto.RoomBriefDto;

@Component
public class HotelRestClient implements HotelClient {

    private final RestClient restClient;

    public HotelRestClient(
            RestClient.Builder builder,
            @Value("${integration.hotel.base-url:http://localhost:8082}") String baseUrl
    ) {
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    public List<RoomBriefDto> recommendRooms(LocalDate startDate, LocalDate endDate) {
        return restClient.get()
                .uri(uri -> uri.path("/api/rooms/recommended")
                        .queryParam("startDate", startDate)
                        .queryParam("endDate", endDate)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<RoomBriefDto>>() {});
    }

    @Override
    public void releaseRoom(Long roomId, String bookingId, String requestId) {
        restClient.post()
                .uri("/api/rooms/{id}/release", roomId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ReleaseRequest(bookingId, requestId))
                .retrieve()
                .toBodilessEntity();
    }

    private record ReleaseRequest(String bookingId, String requestId) {}
}

