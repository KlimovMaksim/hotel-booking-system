package ru.klimov.controller.payload;

import lombok.Data;

@Data
public class RoomPayload {

    private String number;

    private Boolean available;

    private Integer timeBooked;
}
