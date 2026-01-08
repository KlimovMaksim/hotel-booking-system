package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.klimov.controller.payload.HotelPayload;
import ru.klimov.entity.Hotel;
import ru.klimov.entity.Room;
import ru.klimov.repository.HotelRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public Optional<Hotel> getHotelById(UUID id) {
        return hotelRepository.findById(id);
    }

    public Hotel createHotel(HotelPayload hotelPayload) {
        Hotel hotel = new Hotel();
        hotel.setName(hotelPayload.getName());
        hotel.setAddress(hotelPayload.getAddress());
        if (hotelPayload.getRooms() != null) {
            List<Room> rooms = hotelPayload.getRooms().stream().map(roomPayload -> {
                Room room = new Room();
                room.setNumber(roomPayload.getNumber());
                room.setAvailable(roomPayload.getAvailable());
                room.setTimeBooked(roomPayload.getTimeBooked());
                room.setHotel(hotel);
                return room;
            }).collect(Collectors.toList());
            hotel.setRooms(rooms);
        }
        return hotelRepository.save(hotel);
    }

    public Hotel updateHotel(UUID id, Hotel hotelDetails) {
        return hotelRepository.findById(id).map(hotel -> {
            hotel.setName(hotelDetails.getName());
            hotel.setAddress(hotelDetails.getAddress());
            hotel.setRooms(hotelDetails.getRooms());
            return hotelRepository.save(hotel);
        }).orElseThrow(() -> new NoSuchElementException("Hotel not found with id " + id));
    }

    public void deleteHotel(UUID id) {
        hotelRepository.deleteById(id);
    }
}
