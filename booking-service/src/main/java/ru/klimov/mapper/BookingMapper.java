package ru.klimov.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.klimov.dto.BookingDto;
import ru.klimov.dto.BookingResult;
import ru.klimov.dto.RoomReservationDto;
import ru.klimov.entity.Booking;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingDto toDto(Booking booking);

    RoomReservationDto toRoomReservationDto(Booking booking);

    @Mapping(target = "message", constant = "Booking created successfully")
    @Mapping(target = "success", constant = "true")
    BookingResult toBookingResultDto(Booking booking);
}
