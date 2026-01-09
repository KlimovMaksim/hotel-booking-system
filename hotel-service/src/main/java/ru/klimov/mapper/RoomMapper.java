package ru.klimov.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.klimov.dto.RoomDto;
import ru.klimov.entity.Room;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(target = "hotelId", source = "hotel.id")
    @Mapping(target = "hotelName", source = "hotel.name")
    RoomDto toDto(Room room);
}
