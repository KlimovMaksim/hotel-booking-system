package ru.klimov.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class BookingResult {

    private Boolean success;

    private String message;

    private UUID requestId;

    private UUID roomId;

    private LocalDate startDate;

    private LocalDate endDate;
}
