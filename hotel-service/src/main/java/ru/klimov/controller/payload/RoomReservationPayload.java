package ru.klimov.controller.payload;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RoomReservationPayload {
    private LocalDate startDate;
    private LocalDate endDate;
    private String requestId;
}
