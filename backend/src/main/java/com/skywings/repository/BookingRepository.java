package com.skywings.repository;

import com.skywings.entity.Booking;
import com.skywings.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.createdAt < :cutoff")
    List<Booking> findByStatusAndCreatedAtBefore(@Param("status") BookingStatus status,
                                                  @Param("cutoff") LocalDateTime cutoff);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT b FROM Booking b JOIN FETCH b.flight JOIN FETCH b.user ORDER BY b.createdAt DESC")
    List<Booking> findAllWithDetails();

    @Query("SELECT b FROM Booking b JOIN FETCH b.flight JOIN FETCH b.user " +
           "WHERE b.status = :status ORDER BY b.createdAt DESC")
    List<Booking> findAllByStatusWithDetails(@Param("status") BookingStatus status);
}
