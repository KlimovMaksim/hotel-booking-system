package ru.klimov.controller.payload;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BookingPayload {

    private UUID roomId;

    private UUID userId;

    private LocalDate startDate;

    private LocalDate endDate;
}
