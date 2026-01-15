package ru.klimov.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.klimov.controller.payload.HotelPayload;
import ru.klimov.controller.payload.RoomPayload;
import ru.klimov.dto.HotelDto;
import ru.klimov.entity.Hotel;
import ru.klimov.mapper.HotelMapper;
import ru.klimov.repository.HotelRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper mapper;

    @InjectMocks
    private HotelService hotelService;

    @Test
    void getAllHotels_ReturnsListOfHotels() {
        // given
        Hotel hotel1 = new Hotel();
        Hotel hotel2 = new Hotel();
        List<Hotel> hotels = Arrays.asList(hotel1, hotel2);
        
        HotelDto dto1 = new HotelDto();
        HotelDto dto2 = new HotelDto();

        when(hotelRepository.findAll()).thenReturn(hotels);
        when(mapper.toDto(hotel1)).thenReturn(dto1);
        when(mapper.toDto(hotel2)).thenReturn(dto2);

        // when
        List<HotelDto> result = hotelService.getAllHotels();

        // then
        assertThat(result).hasSize(2).containsExactly(dto1, dto2);
        verify(hotelRepository).findAll();
        verify(mapper, times(2)).toDto(any(Hotel.class));
    }

    @Test
    void createHotel_WithRooms_ReturnsCreatedHotelDto() {
        // given
        HotelPayload payload = new HotelPayload();
        payload.setName("Test Hotel");
        payload.setAddress("Test Address");
        
        RoomPayload roomPayload = new RoomPayload();
        roomPayload.setNumber("101");
        roomPayload.setAvailable(true);
        payload.setRooms(Collections.singletonList(roomPayload));

        Hotel savedHotel = new Hotel();
        HotelDto expectedDto = new HotelDto();

        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);
        when(mapper.toDto(savedHotel)).thenReturn(expectedDto);

        // when
        HotelDto result = hotelService.createHotel(payload);

        // then
        assertThat(result).isEqualTo(expectedDto);
        verify(hotelRepository).save(argThat(hotel -> 
            hotel.getName().equals("Test Hotel") &&
            hotel.getAddress().equals("Test Address") &&
            hotel.getRooms().size() == 1 &&
            hotel.getRooms().get(0).getNumber().equals("101") &&
            hotel.getRooms().get(0).getAvailable()
        ));
        verify(mapper).toDto(savedHotel);
    }

    @Test
    void createHotel_WithoutRooms_ReturnsCreatedHotelDto() {
        // given
        HotelPayload payload = new HotelPayload();
        payload.setName("Hotel No Rooms");
        payload.setAddress("Address");
        payload.setRooms(null);

        Hotel savedHotel = new Hotel();
        HotelDto expectedDto = new HotelDto();

        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);
        when(mapper.toDto(savedHotel)).thenReturn(expectedDto);

        // when
        HotelDto result = hotelService.createHotel(payload);

        // then
        assertThat(result).isEqualTo(expectedDto);
        verify(hotelRepository).save(argThat(hotel -> 
            hotel.getName().equals("Hotel No Rooms") &&
            hotel.getRooms() == null
        ));
    }
}
