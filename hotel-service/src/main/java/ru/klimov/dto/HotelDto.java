package ru.klimov.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelDto {

    private UUID id;

    private String name;

    private String address;

    private List<UUID> rooms;
}
