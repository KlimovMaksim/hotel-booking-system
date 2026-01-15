package ru.klimov.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.klimov.dto.HotelDto;
import ru.klimov.entity.Hotel;
import ru.klimov.entity.Room;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    @Mapping(target = "rooms", source = "rooms", qualifiedByName = "roomsToIds")
    HotelDto toDto(Hotel hotel);

    @Named("roomsToIds")
    static List<UUID> roomsToIds(List<Room> rooms) {
        return rooms.stream()
                .map(Room::getId)
                .toList();
    }
}
