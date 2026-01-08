package ru.klimov.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.klimov.controller.payload.BookingPayload;
import ru.klimov.entity.Booking;
import ru.klimov.service.BookingService;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @GetMapping
    public Iterable<Booking> findAll() {
        return bookingService.findAll();
    }

    @GetMapping("/by-username/{username}")
    public Iterable<Booking> findAllByUsername(@PathVariable String username) {
        return bookingService.findAllByUsername(username);
    }

    @PostMapping
    public Booking create(@RequestBody BookingPayload payload) {
        return bookingService.create(payload);
    }

    @GetMapping("/offers")
    public Object getOffers() {
        return bookingService.getOffers();
    }
}
