package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.klimov.controller.payload.RoomPayload;
import ru.klimov.dto.RoomDto;
import ru.klimov.entity.Hotel;
import ru.klimov.entity.Room;
import ru.klimov.mapper.RoomMapper;
import ru.klimov.repository.HotelRepository;
import ru.klimov.repository.RoomRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    public List<RoomDto> getAllRooms() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toDto)
                .toList();
    }

    public Optional<Room> getRoomById(UUID id) {
        return roomRepository.findById(id);
    }

    public RoomDto createRoom(RoomPayload roomPayload) {

        Hotel hotel = hotelRepository.findById(roomPayload.getHotelId())
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found with id " + roomPayload.getHotelId()));

        Room room = new Room();
        room.setNumber(roomPayload.getNumber());
        room.setAvailable(roomPayload.getAvailable());
        room.setTimeBooked(roomPayload.getTimeBooked());
        room.setHotel(hotel);

        return roomMapper.toDto(roomRepository.save(room));
    }

    public RoomDto updateRoom(UUID id, Room roomDetails) {
        return roomRepository.findById(id).map(room -> {
            room.setNumber(roomDetails.getNumber());
            room.setAvailable(roomDetails.getAvailable());
            room.setTimeBooked(roomDetails.getTimeBooked());
            room.setHotel(roomDetails.getHotel());
            return roomMapper.toDto(roomRepository.save(room));
        }).orElseThrow(() -> new NoSuchElementException("Room not found with id " + id));
    }

    public void deleteRoom(UUID id) {
        roomRepository.deleteById(id);
    }

    public List<RoomDto> getRecommendRooms() {
        return roomRepository.findAllByOrderByTimeBookedDesc().stream()
                .map(roomMapper::toDto)
                .toList();
    }

    public List<RoomDto> getAllAvailableRooms() {
        return roomRepository.findAll().stream()
                .filter(Room::getAvailable)
                .map(roomMapper::toDto)
                .toList();
    }
}
