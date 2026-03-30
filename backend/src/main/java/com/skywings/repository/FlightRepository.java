package com.skywings.repository;

import com.skywings.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {

    Optional<Flight> findByFlightNumber(String flightNumber);

    boolean existsByFlightNumber(String flightNumber);

    @Query("SELECT f FROM Flight f WHERE f.originCode = :origin AND f.destCode = :dest " +
           "AND CAST(f.departureTime AS localdate) = :date AND f.status = 'SCHEDULED' " +
           "ORDER BY f.departureTime")
    List<Flight> searchFlights(@Param("origin") String origin,
                               @Param("dest") String dest,
                               @Param("date") LocalDate date);

    @Query("SELECT f FROM Flight f WHERE f.status = 'SCHEDULED' AND f.departureTime > CURRENT_TIMESTAMP " +
           "ORDER BY f.departureTime")
    List<Flight> findUpcomingFlights();
}
