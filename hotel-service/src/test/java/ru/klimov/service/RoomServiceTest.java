package ru.klimov.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.klimov.controller.payload.RoomPayload;
import ru.klimov.dto.RoomDto;
import ru.klimov.entity.Hotel;
import ru.klimov.entity.Room;
import ru.klimov.mapper.RoomMapper;
import ru.klimov.repository.HotelRepository;
import ru.klimov.repository.RoomRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private RoomMapper roomMapper;

    @InjectMocks
    private RoomService roomService;

    @Test
    void getRoomById_ReturnsRoom() {
        // given
        UUID roomId = UUID.randomUUID();
        Room room = new Room();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        // when
        Optional<Room> result = roomService.getRoomById(roomId);

        // then
        assertThat(result).isPresent().contains(room);
        verify(roomRepository).findById(roomId);
    }

    @Test
    void createRoom_HotelFound_ReturnsCreatedRoomDto() {
        // given
        UUID hotelId = UUID.randomUUID();
        RoomPayload payload = new RoomPayload();
        payload.setHotelId(hotelId);
        payload.setNumber("101");
        payload.setAvailable(true);

        Hotel hotel = new Hotel();
        Room savedRoom = new Room();
        RoomDto expectedDto = new RoomDto();

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(roomRepository.save(any(Room.class))).thenReturn(savedRoom);
        when(roomMapper.toDto(savedRoom)).thenReturn(expectedDto);

        // when
        RoomDto result = roomService.createRoom(payload);

        // then
        assertThat(result).isEqualTo(expectedDto);
        verify(hotelRepository).findById(hotelId);
        verify(roomRepository).save(argThat(room -> 
            room.getNumber().equals("101") &&
            room.getAvailable() &&
            room.getHotel().equals(hotel)
        ));
    }

    @Test
    void createRoom_HotelNotFound_ThrowsException() {
        // given
        UUID hotelId = UUID.randomUUID();
        RoomPayload payload = new RoomPayload();
        payload.setHotelId(hotelId);

        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> roomService.createRoom(payload))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Hotel not found with id " + hotelId);
        
        verify(roomRepository, never()).save(any());
    }

    @Test
    void getRecommendRooms_ReturnsSortedRooms() {
        // given
        Room room1 = new Room();
        Room room2 = new Room();
        List<Room> rooms = Arrays.asList(room1, room2);
        
        RoomDto dto1 = new RoomDto();
        RoomDto dto2 = new RoomDto();

        when(roomRepository.findAllByOrderByTimeBookedDesc()).thenReturn(rooms);
        when(roomMapper.toDto(room1)).thenReturn(dto1);
        when(roomMapper.toDto(room2)).thenReturn(dto2);

        // when
        List<RoomDto> result = roomService.getRecommendRooms();

        // then
        assertThat(result).hasSize(2).containsExactly(dto1, dto2);
        verify(roomRepository).findAllByOrderByTimeBookedDesc();
    }

    @Test
    void getAllAvailableRooms_ReturnsOnlyAvailableRooms() {
        // given
        Room availableRoom = new Room();
        availableRoom.setAvailable(true);
        Room unavailableRoom = new Room();
        unavailableRoom.setAvailable(false);
        
        List<Room> allRooms = Arrays.asList(availableRoom, unavailableRoom);
        RoomDto availableDto = new RoomDto();

        when(roomRepository.findAll()).thenReturn(allRooms);
        when(roomMapper.toDto(availableRoom)).thenReturn(availableDto);

        // when
        List<RoomDto> result = roomService.getAllAvailableRooms();

        // then
        assertThat(result).hasSize(1).containsExactly(availableDto);
        verify(roomRepository).findAll();
        verify(roomMapper).toDto(availableRoom);
        verify(roomMapper, never()).toDto(unavailableRoom);
    }
}
