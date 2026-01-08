package ru.klimov.controller.payload;

import lombok.Data;

import java.util.List;

@Data
public class HotelPayload {

    private String name;

    private String address;

    private List<RoomPayload> rooms;
}
