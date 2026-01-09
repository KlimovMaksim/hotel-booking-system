package ru.klimov.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
public class RoomReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private UUID requestId;

    private LocalDate startDate;

    private LocalDate endDate;

    @ManyToOne
    private Room room;

    @Enumerated(EnumType.STRING)
    private RoomStatus status;
}
