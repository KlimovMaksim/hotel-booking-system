package ru.klimov;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import ru.klimov.entity.Hotel;
import ru.klimov.entity.Room;
import ru.klimov.repository.HotelRepository;
import ru.klimov.repository.RoomRepository;

import java.util.List;

@SpringBootApplication
@EnableDiscoveryClient
public class HotelServiceLauncher {

    public static void main(String[] args) {
        SpringApplication.run(HotelServiceLauncher.class, args);
    }

    @Bean
    public CommandLineRunner testData(HotelRepository hotelRepository, RoomRepository roomRepository) {
        return args -> {
            Hotel hotel = new Hotel();
            hotel.setName("Grand Hotel");
            hotel.setAddress("123 Main St");

            Room room1 = new Room();
            room1.setNumber("101");
            room1.setAvailable(true);
            room1.setHotel(hotel);
            room1.setTimeBooked(4);

            Room room2 = new Room();
            room2.setNumber("102");
            room2.setAvailable(true);
            room2.setHotel(hotel);
            room2.setTimeBooked(6);

            hotel.setRooms(List.of(room1, room2));

            hotelRepository.save(hotel);
        };
    }
}
