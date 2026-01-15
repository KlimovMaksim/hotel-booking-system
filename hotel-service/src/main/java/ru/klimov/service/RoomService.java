package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;

    public Optional<Room> getRoomById(UUID id) {
        log.info("Fetching room by id: {}", id);
        return roomRepository.findById(id);
    }

    public RoomDto createRoom(RoomPayload roomPayload) {
        log.info("Creating room number {} for hotel id {}", roomPayload.getNumber(), roomPayload.getHotelId());

        Hotel hotel = hotelRepository.findById(roomPayload.getHotelId())
                .orElseThrow(() -> {
                    log.error("Room creation failed: hotel {} not found", roomPayload.getHotelId());
                    return new IllegalArgumentException("Hotel not found with id " + roomPayload.getHotelId());
                });

        Room room = new Room();
        room.setNumber(roomPayload.getNumber());
        room.setAvailable(roomPayload.getAvailable());
        room.setTimeBooked(0);
        room.setHotel(hotel);

        Room savedRoom = roomRepository.save(room);
        log.info("Room {} successfully created for hotel {}", savedRoom.getNumber(), hotel.getName());
        return roomMapper.toDto(savedRoom);
    }

    public List<RoomDto> getRecommendRooms() {
        log.info("Fetching recommended rooms");
        return roomRepository.findAllByOrderByTimeBookedDesc().stream()
                .map(roomMapper::toDto)
                .toList();
    }

    public List<RoomDto> getAllAvailableRooms() {
        log.info("Fetching all available rooms");
        return roomRepository.findAll().stream()
                .filter(Room::getAvailable)
                .map(roomMapper::toDto)
                .toList();
    }
}
