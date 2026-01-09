package ru.klimov.controller.payload;

import lombok.Data;

import java.util.UUID;

@Data
public class RoomPayload {

    private String number;

    private Boolean available;

    private Integer timeBooked;

    private UUID hotelId;
}
