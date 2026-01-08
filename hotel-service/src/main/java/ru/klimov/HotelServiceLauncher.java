package ru.klimov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class HotelServiceLauncher {

    public static void main(String[] args) {
        SpringApplication.run(HotelServiceLauncher.class, args);
    }
}
