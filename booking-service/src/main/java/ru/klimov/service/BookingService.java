package ru.klimov.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final BookingMapper bookingMapper;

    public Iterable<BookingDto> findAll() {
        log.info("Fetching all bookings");
        return bookingRepository.findAll().stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    public Iterable<BookingDto> findAllByUsername(String username) {
        log.info("Fetching bookings for user: {}", username);
        validateAccess(username);
        return bookingRepository.findAllByUserUsername(username).stream()
                .map(bookingMapper::toDto)
                .toList();
    }

    private static void validateAccess(String username) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        if (!isAdmin && !currentUsername.equals(username)) {
            log.warn("Access denied for user {} trying to access bookings of {}", currentUsername, username);
            throw new AccessDeniedException("You can only access your own bookings");
        }
    }

    public BookingDto cancelBooking(UUID requestId) {
        log.info("Cancelling booking with requestId: {}", requestId);
        Booking booking = bookingRepository.findByRequestId(requestId)
                .orElseThrow(() -> {
                    log.error("Booking not found with requestId: {}", requestId);
                    return new IllegalArgumentException("Booking not found with id " + requestId);
                });

        String username = booking.getUser().getUsername();
        validateAccess(username);

        UUID roomId = booking.getRoomId();
        restTemplate.postForObject(
                "http://hotel-service/rooms/{id}/release/{requestId}",
                null,
                Void.class,
                roomId,
                requestId
        );

        booking.setStatus(BookingStatus.CANCELLED);
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking {} successfully cancelled", requestId);
        return bookingMapper.toDto(savedBooking);
    }

    public BookingResult create(BookingPayload payload) {
        log.info("Creating booking for roomId: {}", payload.getRoomId());
        validateDates(payload);

        Booking booking = createBooking(payload);

        RoomReservationDto roomReservationDto = bookingMapper.toRoomReservationDto(booking);

        Boolean isConfirmed = restTemplate.postForObject(
                "http://hotel-service/rooms/{id}/confirm-availability",
                roomReservationDto,
                Boolean.class,
                booking.getRoomId()
        );

        if (Boolean.TRUE.equals(isConfirmed)) {
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);
            log.info("Booking {} confirmed for room {}", booking.getRequestId(), booking.getRoomId());
            return bookingMapper.toBookingResultDto(booking);
        } else {
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            log.warn("Booking {} cancelled: room {} not available", booking.getRequestId(), booking.getRoomId());
            return BookingResult.builder()
                    .success(false)
                    .message("Room is not available")
                    .build();
        }
    }

    private void validateDates(BookingPayload payload) {
        if (payload.getStartDate().isAfter(payload.getEndDate())) {
            log.error("Invalid dates: startDate {} is after endDate {}", payload.getStartDate(), payload.getEndDate());
            throw new IllegalArgumentException("StartDate must be before EndDate");
        }
    }

    private Booking createBooking(BookingPayload payload) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new EntityNotFoundException("User not found with username " + username);
                });

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
        
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created in PENDING status: {}", requestId);
        return savedBooking;
    }

    private UUID retrieveRoomId(BookingPayload payload) {
        UUID roomId;
        if (payload.getAutoSelect()) {
            log.info("Auto-selecting room");
            List<RoomDto> offers = getOffers();
            if (offers.isEmpty()) {
                log.error("No rooms available for auto-selection");
                throw new EntityNotFoundException("No rooms available");
            }
            roomId = offers.get(0).getId();
            log.info("Auto-selected roomId: {}", roomId);
        } else {
            roomId = ofNullable(payload.getRoomId())
                    .orElseThrow(() -> new IllegalArgumentException("RoomId is required"));
        }
        return roomId;
    }

    public List<RoomDto> getOffers() {
        log.info("Fetching room recommendations from hotel-service");
        RoomDto[] rooms = restTemplate.getForObject("http://hotel-service/rooms/recommend", RoomDto[].class);
        return rooms != null ? Arrays.asList(rooms) : List.of();
    }

    public BookingDto findByRequestId(UUID requestId) {
        log.info("Finding booking by requestId: {}", requestId);
        return bookingRepository.findByRequestId(requestId)
                .map(bookingMapper::toDto)
                .orElseThrow(() -> {
                    log.error("Booking not found with requestId: {}", requestId);
                    return new IllegalArgumentException("Booking not found with id " + requestId);
                });
    }
}
