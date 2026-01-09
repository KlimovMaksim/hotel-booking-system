package ru.klimov.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.klimov.entity.Room;
import ru.klimov.entity.RoomReservation;
import ru.klimov.entity.RoomStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoomReservationRepository extends JpaRepository<RoomReservation, UUID> {

    Optional<RoomReservation> findByRequestId(UUID requestId);

    List<RoomReservation> findByRoomAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Room room,
            RoomStatus status,
            LocalDate startDate,
            LocalDate endDate);
}
