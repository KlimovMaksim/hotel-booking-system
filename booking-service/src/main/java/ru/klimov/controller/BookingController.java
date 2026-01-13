package ru.klimov.controller;

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

    @GetMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public Iterable<BookingDto> findAll() {
        return bookingService.findAll();
    }

    @GetMapping("/by-username/{username}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public Iterable<BookingDto> findAllByUsername(@PathVariable("username") String username) {
        return bookingService.findAllByUsername(username);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public BookingResult create(@RequestBody BookingPayload payload) {
        return bookingService.create(payload);
    }

    @GetMapping("/offers")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public List<RoomDto> getOffers() {
        return bookingService.getOffers();
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public BookingDto findByRequestId(@PathVariable("requestId") UUID requestId) {
        return bookingService.findByRequestId(requestId);
    }

    @DeleteMapping("/{requestId}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'USER')")
    public BookingDto cancelBooking(@PathVariable("requestId") UUID requestId) {
        return bookingService.cancelBooking(requestId);
    }
}
