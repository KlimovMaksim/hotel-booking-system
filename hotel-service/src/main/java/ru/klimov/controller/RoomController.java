package ru.klimov.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.klimov.controller.payload.RoomPayload;
import ru.klimov.controller.payload.RoomReservationPayload;
import ru.klimov.dto.RoomDto;
import ru.klimov.service.RoomReservationService;
import ru.klimov.service.RoomService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomReservationService roomReservationService;

    @Operation(summary = "Создать новый номер")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public RoomDto createRoom(@RequestBody RoomPayload roomPayload) {
        return roomService.createRoom(roomPayload);
    }

    @Operation(summary = "Получить рекомендованные номера")
    @GetMapping("/recommend")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<RoomDto> getRecommendRooms() {
        return roomService.getRecommendRooms();
    }

    @Operation(summary = "Получить все доступные номера")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public List<RoomDto> getAllAvailableRooms() {
        return roomService.getAllAvailableRooms();
    }

    @Operation(summary = "Подтвердить доступность номера")
    @PostMapping("/{id}/confirm-availability")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public boolean confirmAvailability(@PathVariable("id") UUID id, @RequestBody RoomReservationPayload payload) {
        return roomReservationService.confirmAvailability(id, payload);
    }

    @Operation(summary = "Освободить номер")
    @PostMapping("/{id}/release/{requestId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public void releaseRoom(@PathVariable("id") UUID id, @PathVariable("requestId") UUID requestId) {
        roomReservationService.releaseRoom(requestId);
    }
}
