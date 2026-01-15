package ru.klimov.controller.payload;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class HotelPayload {

    @Schema(description = "Название отеля", example = "Grand Hotel")
    private String name;

    @Schema(description = "Адрес отеля", example = "ул. Ленина, 1")
    private String address;

    @Schema(description = "Список номеров отеля")
    private List<RoomPayload> rooms;
}
