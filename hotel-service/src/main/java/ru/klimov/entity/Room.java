package ru.klimov.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String number;

    private Boolean available;

    private Integer timeBooked;

    @ManyToOne
    private Hotel hotel;
}
