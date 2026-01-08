package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.klimov.controller.payload.BookingPayload;
import ru.klimov.entity.Booking;
import ru.klimov.entity.BookingStatus;
import ru.klimov.repository.BookingRepository;
import ru.klimov.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public Iterable<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Iterable<Booking> findAllByUsername(String username) {
        return bookingRepository.findAllByUserUsername(username);
    }

    public Booking create(BookingPayload payload) {
        Booking booking = new Booking();
        booking.setRequestId(UUID.randomUUID());
        booking.setRoomId(payload.getRoomId());
        booking.setStartDate(payload.getStartDate());
        booking.setEndDate(payload.getEndDate());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setCreatedAt(LocalDateTime.now());
        
        userRepository.findById(payload.getUserId()).ifPresent(booking::setUser);

        return bookingRepository.save(booking);
    }

    public Object getOffers() {
        // todo реализовать на этапе интеграции с hotel-service
        return null;
    }
}
