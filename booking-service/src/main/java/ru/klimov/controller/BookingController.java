package ru.klimov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.klimov.controller.payload.BookingPayload;
import ru.klimov.dto.BookingDto;
import ru.klimov.dto.BookingResult;
import ru.klimov.dto.RoomDto;
import ru.klimov.service.BookingService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public Iterable<BookingDto> findAll() {
        return bookingService.findAll();
    }

    @GetMapping("/by-username/{username}")
    public Iterable<BookingDto> findAllByUsername(@PathVariable("username") String username) {
        return bookingService.findAllByUsername(username);
    }

    @PostMapping
    public BookingResult create(@RequestBody BookingPayload payload) {
        return bookingService.create(payload);
    }

    @GetMapping("/offers")
    public List<RoomDto> getOffers() {
        return bookingService.getOffers();
    }

    @GetMapping("/{requestId}")
    public BookingDto findByRequestId(@PathVariable("requestId") UUID requestId) {
        return bookingService.findByRequestId(requestId);
    }

    @DeleteMapping("/{requestId}")
    public BookingDto cancelBooking(@PathVariable("requestId") UUID requestId) {
        return bookingService.cancelBooking(requestId);
    }
}
