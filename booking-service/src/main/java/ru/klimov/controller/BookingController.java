package ru.klimov.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.klimov.controller.payload.BookingPayload;
import ru.klimov.dto.BookingDto;
import ru.klimov.dto.BookingResult;
import ru.klimov.dto.RoomDto;
import ru.klimov.service.BookingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @Operation(summary = "Получить список всех бронирований")
    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Iterable<BookingDto> findAll() {
        return bookingService.findAll();
    }

    @Operation(summary = "Получить список бронирований по имени пользователя")
    @GetMapping("/by-username/{username}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public Iterable<BookingDto> findAllByUsername(@PathVariable("username") String username) {
        return bookingService.findAllByUsername(username);
    }

    @Operation(summary = "Создать новое бронирование")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public BookingResult create(@RequestBody BookingPayload payload) {
        return bookingService.create(payload);
    }

    @Operation(summary = "Получить доступные предложения номеров")
    @GetMapping("/offers")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public List<RoomDto> getOffers() {
        return bookingService.getOffers();
    }

    @Operation(summary = "Найти бронирование по идентификатору запроса")
    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public BookingDto findByRequestId(@PathVariable("requestId") UUID requestId) {
        return bookingService.findByRequestId(requestId);
    }

    @Operation(summary = "Отменить бронирование")
    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public BookingDto cancelBooking(@PathVariable("requestId") UUID requestId) {
        return bookingService.cancelBooking(requestId);
    }
}
