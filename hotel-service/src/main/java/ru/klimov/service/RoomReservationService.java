package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.klimov.controller.payload.RoomReservationPayload;
import ru.klimov.entity.Room;
import ru.klimov.entity.RoomReservation;
import ru.klimov.entity.RoomStatus;
import ru.klimov.repository.RoomRepository;
import ru.klimov.repository.RoomReservationRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomReservationService {

    private final RoomService roomService;
    private final RoomReservationRepository roomReservationRepository;
    private final RoomRepository roomRepository;

    public boolean confirmAvailability(UUID roomId, RoomReservationPayload reservationPayload) {
        log.info("Checking availability for roomId: {} and requestId: {}", roomId, reservationPayload.getRequestId());

        Room room = roomService.getRoomById(roomId).orElseThrow(() -> {
            log.error("Availability check failed: room {} not found", roomId);
            return new IllegalArgumentException("Room not found");
        });

        UUID requestId = UUID.fromString(reservationPayload.getRequestId());
        Optional<RoomReservation> optionalRoomReservation = roomReservationRepository.findByRequestId(requestId);
        if (optionalRoomReservation.isPresent()) {
            log.warn("Reservation with requestId {} already exists", requestId);
            return false;
        }

        List<RoomReservation> roomReservations = roomReservationRepository.findByRoomAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                room,
                RoomStatus.CONFIRMED,
                reservationPayload.getStartDate(),
                reservationPayload.getEndDate()
        );

        if (!roomReservations.isEmpty()) {
            log.warn("Room {} is already booked for the specified dates", roomId);
            return false;
        }

        RoomReservation roomReservation = new RoomReservation();
        roomReservation.setRequestId(requestId);
        roomReservation.setRoom(room);
        roomReservation.setStartDate(reservationPayload.getStartDate());
        roomReservation.setEndDate(reservationPayload.getEndDate());
        roomReservation.setStatus(RoomStatus.CONFIRMED);
        roomReservationRepository.save(roomReservation);
        
        room.setTimeBooked(room.getTimeBooked() + 1);
        roomRepository.save(room);
        
        log.info("Room {} successfully reserved for requestId {}", roomId, requestId);
        return true;
    }

    public void releaseRoom(UUID requestId) {
        log.info("Releasing room for requestId: {}", requestId);
        RoomReservation roomReservation = roomReservationRepository.findByRequestId(requestId)
                .orElseThrow(() -> {
                    log.error("Release failed: reservation not found for requestId {}", requestId);
                    return new IllegalArgumentException("Room reservation not found");
                });

        roomReservation.setStatus(RoomStatus.RELEASED);
        roomReservationRepository.save(roomReservation);
        log.info("Room for requestId {} successfully released", requestId);
    }
}
