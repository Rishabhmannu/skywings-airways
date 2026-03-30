package com.skywings.repository;

import com.skywings.entity.Seat;
import com.skywings.entity.enums.SeatClass;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.flight.id = :flightId AND s.seatClass = :seatClass " +
           "AND s.isAvailable = true ORDER BY s.seatNumber")
    List<Seat> findAvailableSeatsWithLock(@Param("flightId") Long flightId,
                                          @Param("seatClass") SeatClass seatClass);

    @Query("SELECT s FROM Seat s WHERE s.flight.id = :flightId AND s.seatClass = :seatClass " +
           "AND s.isAvailable = true ORDER BY s.seatNumber")
    List<Seat> findAvailableSeats(@Param("flightId") Long flightId,
                                  @Param("seatClass") SeatClass seatClass);

    @Query("SELECT s FROM Seat s WHERE s.flight.id = :flightId ORDER BY s.seatClass, s.seatNumber")
    List<Seat> findAllByFlightId(@Param("flightId") Long flightId);

    @Query("SELECT COUNT(s) FROM Seat s WHERE s.flight.id = :flightId AND s.seatClass = :seatClass " +
           "AND s.isAvailable = true")
    long countAvailableSeats(@Param("flightId") Long flightId,
                             @Param("seatClass") SeatClass seatClass);
}
