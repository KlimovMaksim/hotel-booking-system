package ru.klimov.service;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class RoomReservationService {

    private final RoomService roomService;
    private final RoomReservationRepository roomReservationRepository;
    private final RoomRepository roomRepository;

    public boolean confirmAvailability(UUID roomId, RoomReservationPayload reservationPayload) {

        Room room = roomService.getRoomById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found"));

        UUID requestId = UUID.fromString(reservationPayload.getRequestId());
        Optional<RoomReservation> optionalRoomReservation = roomReservationRepository.findByRequestId(requestId);
        if (optionalRoomReservation.isPresent()) return false;

        List<RoomReservation> roomReservations = roomReservationRepository.findByRoomAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                room,
                RoomStatus.CONFIRMED,
                reservationPayload.getStartDate(),
                reservationPayload.getEndDate()
        );

        if (!roomReservations.isEmpty()) return false;

        RoomReservation roomReservation = new RoomReservation();
        roomReservation.setRequestId(requestId);
        roomReservation.setRoom(room);
        roomReservation.setStartDate(reservationPayload.getStartDate());
        roomReservation.setEndDate(reservationPayload.getEndDate());
        roomReservation.setStatus(RoomStatus.CONFIRMED);
        roomReservationRepository.save(roomReservation);
        room.setTimeBooked(room.getTimeBooked() + 1);
        roomRepository.save(room);
        return true;
    }

    public void releaseRoom(UUID requestId) {
        RoomReservation roomReservation = roomReservationRepository.findByRequestId(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Room reservation not found"));

        roomReservation.setStatus(RoomStatus.RELEASED);
        roomReservationRepository.save(roomReservation);
    }
}
