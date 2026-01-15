package ru.klimov.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.klimov.controller.payload.RoomReservationPayload;
import ru.klimov.entity.Room;
import ru.klimov.entity.RoomReservation;
import ru.klimov.entity.RoomStatus;
import ru.klimov.repository.RoomRepository;
import ru.klimov.repository.RoomReservationRepository;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomReservationServiceTest {

    @Mock
    private RoomService roomService;

    @Mock
    private RoomReservationRepository roomReservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private RoomReservationService roomReservationService;

    @Test
    @DisplayName("confirmAvailability выбрасывает исключение, если комната не найдена")
    void confirmAvailability_RoomNotFound_ThrowsException() {
        // given
        UUID roomId = UUID.randomUUID();
        when(roomService.getRoomById(roomId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> roomReservationService.confirmAvailability(roomId, new RoomReservationPayload()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room not found");
    }

    @Test
    @DisplayName("confirmAvailability возвращает false, если бронирование с таким requestId уже существует")
    void confirmAvailability_DuplicateRequestId_ReturnsFalse() {
        // given
        UUID roomId = UUID.randomUUID();
        Room room = new Room();
        UUID requestId = UUID.randomUUID();
        RoomReservationPayload payload = new RoomReservationPayload();
        payload.setRequestId(requestId.toString());

        when(roomService.getRoomById(roomId)).thenReturn(Optional.of(room));
        when(roomReservationRepository.findByRequestId(requestId)).thenReturn(Optional.of(new RoomReservation()));

        // when
        boolean result = roomReservationService.confirmAvailability(roomId, payload);

        // then
        assertThat(result).isFalse();
        verify(roomReservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("confirmAvailability возвращает false, если есть пересекающиеся бронирования")
    void confirmAvailability_OverlappingReservations_ReturnsFalse() {
        // given
        UUID roomId = UUID.randomUUID();
        Room room = new Room();
        UUID requestId = UUID.randomUUID();
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(1);
        
        RoomReservationPayload payload = new RoomReservationPayload();
        payload.setRequestId(requestId.toString());
        payload.setStartDate(start);
        payload.setEndDate(end);

        when(roomService.getRoomById(roomId)).thenReturn(Optional.of(room));
        when(roomReservationRepository.findByRequestId(requestId)).thenReturn(Optional.empty());
        when(roomReservationRepository.findByRoomAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                room, RoomStatus.CONFIRMED, start, end))
                .thenReturn(Collections.singletonList(new RoomReservation()));

        // when
        boolean result = roomReservationService.confirmAvailability(roomId, payload);

        // then
        assertThat(result).isFalse();
        verify(roomReservationRepository, never()).save(any());
    }

    @Test
    void confirmAvailability_Success_ReturnsTrue() {
        // given
        UUID roomId = UUID.randomUUID();
        Room room = new Room();
        room.setTimeBooked(5);
        UUID requestId = UUID.randomUUID();
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusDays(1);
        
        RoomReservationPayload payload = new RoomReservationPayload();
        payload.setRequestId(requestId.toString());
        payload.setStartDate(start);
        payload.setEndDate(end);

        when(roomService.getRoomById(roomId)).thenReturn(Optional.of(room));
        when(roomReservationRepository.findByRequestId(requestId)).thenReturn(Optional.empty());
        when(roomReservationRepository.findByRoomAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                room, RoomStatus.CONFIRMED, start, end))
                .thenReturn(Collections.emptyList());

        // when
        boolean result = roomReservationService.confirmAvailability(roomId, payload);

        // then
        assertThat(result).isTrue();
        assertThat(room.getTimeBooked()).isEqualTo(6);
        verify(roomReservationRepository).save(argThat(res -> 
            res.getRequestId().equals(requestId) &&
            res.getRoom().equals(room) &&
            res.getStartDate().equals(start) &&
            res.getEndDate().equals(end) &&
            res.getStatus() == RoomStatus.CONFIRMED
        ));
        verify(roomRepository).save(room);
    }

    @Test
    void releaseRoom_Success() {
        // given
        UUID requestId = UUID.randomUUID();
        RoomReservation reservation = new RoomReservation();
        reservation.setStatus(RoomStatus.CONFIRMED);

        when(roomReservationRepository.findByRequestId(requestId)).thenReturn(Optional.of(reservation));

        // when
        roomReservationService.releaseRoom(requestId);

        // then
        assertThat(reservation.getStatus()).isEqualTo(RoomStatus.RELEASED);
        verify(roomReservationRepository).save(reservation);
    }

    @Test
    void releaseRoom_NotFound_ThrowsException() {
        // given
        UUID requestId = UUID.randomUUID();
        when(roomReservationRepository.findByRequestId(requestId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> roomReservationService.releaseRoom(requestId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Room reservation not found");
    }
}
