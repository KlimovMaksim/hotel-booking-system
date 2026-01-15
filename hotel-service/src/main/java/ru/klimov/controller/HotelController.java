package ru.klimov.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.klimov.controller.payload.HotelPayload;
import ru.klimov.dto.HotelDto;
import ru.klimov.service.HotelService;

import java.util.List;

@RestController
@RequestMapping("/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @Operation(summary = "Получить список всех отелей")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<HotelDto> getAllHotels() {
        return hotelService.getAllHotels();
    }

    @Operation(summary = "Создать новый отель")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public HotelDto createHotel(@RequestBody HotelPayload hotelPayload) {
        return hotelService.createHotel(hotelPayload);
    }
}
