package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.klimov.controller.payload.RoomPayload;
import ru.klimov.entity.Room;
import ru.klimov.repository.RoomRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public Optional<Room> getRoomById(UUID id) {
        return roomRepository.findById(id);
    }

    public Room createRoom(RoomPayload roomPayload) {
        Room room = new Room();
        room.setNumber(roomPayload.getNumber());
        room.setAvailable(roomPayload.getAvailable());
        room.setTimeBooked(roomPayload.getTimeBooked());
        return roomRepository.save(room);
    }

    public Room updateRoom(UUID id, Room roomDetails) {
        return roomRepository.findById(id).map(room -> {
            room.setNumber(roomDetails.getNumber());
            room.setAvailable(roomDetails.getAvailable());
            room.setTimeBooked(roomDetails.getTimeBooked());
            room.setHotel(roomDetails.getHotel());
            return roomRepository.save(room);
        }).orElseThrow(() -> new NoSuchElementException("Room not found with id " + id));
    }

    public void deleteRoom(UUID id) {
        roomRepository.deleteById(id);
    }
}
