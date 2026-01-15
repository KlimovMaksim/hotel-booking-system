package ru.klimov.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.klimov.controller.payload.HotelPayload;
import ru.klimov.dto.HotelDto;
import ru.klimov.entity.Hotel;
import ru.klimov.entity.Room;
import ru.klimov.mapper.HotelMapper;
import ru.klimov.repository.HotelRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotelService {

    private final HotelRepository hotelRepository;
    private final HotelMapper mapper;

    public List<HotelDto> getAllHotels() {
        log.info("Fetching all hotels");
        return hotelRepository.findAll().stream()
                .map(mapper::toDto)
                .toList();
    }

    public HotelDto createHotel(HotelPayload hotelPayload) {
        log.info("Creating hotel with name: {}", hotelPayload.getName());
        Hotel hotel = new Hotel();
        hotel.setName(hotelPayload.getName());
        hotel.setAddress(hotelPayload.getAddress());
        if (hotelPayload.getRooms() != null) {
            log.info("Adding {} rooms to hotel {}", hotelPayload.getRooms().size(), hotelPayload.getName());
            List<Room> rooms = hotelPayload.getRooms().stream().map(roomPayload -> {
                Room room = new Room();
                room.setNumber(roomPayload.getNumber());
                room.setAvailable(roomPayload.getAvailable());
                room.setTimeBooked(0);
                room.setHotel(hotel);
                return room;
            }).collect(Collectors.toList());
            hotel.setRooms(rooms);
        }
        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Hotel {} successfully created with id {}", savedHotel.getName(), savedHotel.getId());
        return mapper.toDto(savedHotel);
    }
}
