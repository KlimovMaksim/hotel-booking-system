package ru.klimov.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoomDto {

    private UUID id;

    private String number;

    private Boolean available;

    private Integer timeBooked;

    private UUID hotelId;

    private String hotelName;
}
