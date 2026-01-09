package ru.klimov.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.klimov.entity.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDto {

    private UUID id;

    private UUID roomId;

    private LocalDate startDate;

    private LocalDate endDate;

    private BookingStatus status;

    private LocalDateTime createdAt;
}
