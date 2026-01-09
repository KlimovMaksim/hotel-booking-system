package ru.klimov.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.klimov.controller.payload.BookingPayload;
import ru.klimov.dto.BookingDto;
import ru.klimov.dto.BookingResult;
import ru.klimov.dto.RoomDto;
import ru.klimov.dto.RoomReservationDto;
import ru.klimov.entity.Booking;
import ru.klimov.entity.BookingStatus;
import ru.klimov.entity.User;
import ru.klimov.mapper.BookingMapper;
import ru.klimov.repository.BookingRepository;
import ru.klimov.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final BookingMapper bookingMapper;

    public Iterable<BookingDto> findAll() {
        return bookingRepository.findAll().stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    public Iterable<BookingDto> findAllByUsername(String username) {
        return bookingRepository.findAllByUserUsername(username).stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    public BookingDto cancelBooking(UUID requestId) {
        Booking booking = bookingRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id " + requestId));

        UUID roomId = booking.getRoomId();
        restTemplate.postForObject(
                "http://hotel-service/api/rooms/{id}/release/{requestId}",
                null,
                Void.class,
                roomId,
                requestId
        );

        booking.setStatus(BookingStatus.CANCELLED);
        return bookingMapper.toDto(bookingRepository.save(booking));
    }

    public BookingResult create(BookingPayload payload) {

        Booking booking = createBooking(payload);

        RoomReservationDto roomReservationDto = bookingMapper.toRoomReservationDto(booking);

        Boolean isConfirmed = restTemplate.postForObject(
                "http://hotel-service/api/rooms/{id}/confirm-availability",
                roomReservationDto,
                Boolean.class,
                booking.getRoomId()
        );

        if (Boolean.TRUE.equals(isConfirmed)) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            return bookingMapper.toBookingResultDto(booking);
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            return BookingResult.builder()
                    .success(false)
                    .message("Room is not available")
                    .build();
        }
    }

    private Booking createBooking(BookingPayload payload) {
        UUID userId = ofNullable(payload.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("UserId is required"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id " + userId));

        UUID roomId = retrieveRoomId(payload);

        LocalDate startDate = ofNullable(payload.getStartDate())
                .orElseThrow(() -> new IllegalArgumentException("StartDate is required"));
        LocalDate endDate = ofNullable(payload.getEndDate())
                .orElseThrow(() -> new IllegalArgumentException("EndDate is required"));

        UUID requestId = UUID.randomUUID();

        Booking booking = new Booking();
        booking.setRequestId(requestId);
        booking.setRoomId(roomId);
        booking.setUser(user);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        booking.setStatus(BookingStatus.PENDING);
        booking.setCreatedAt(LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    private UUID retrieveRoomId(BookingPayload payload) {
        UUID roomId;
        if (payload.getAutoSelect()) {
            List<RoomDto> offers = getOffers();
            roomId = offers.get(0).getId();
        } else {
            roomId = ofNullable(payload.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("RoomId is required"));
        }
        return roomId;
    }

    public List<RoomDto> getOffers() {
        RoomDto[] rooms = restTemplate.getForObject("http://hotel-service/api/rooms/recommend", RoomDto[].class);
        return rooms != null ? Arrays.asList(rooms) : List.of();
    }

    public BookingDto findByRequestId(UUID requestId) {
        return bookingRepository.findByRequestId(requestId)
                .map(bookingMapper::toDto)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id " + requestId));
    }
}
