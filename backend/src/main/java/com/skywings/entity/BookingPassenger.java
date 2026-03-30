package com.skywings.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_passengers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"booking_id", "seat_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingPassenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Seat seat;

    @Column(name = "passenger_name", nullable = false, length = 100)
    private String passengerName;

    @Column(nullable = false)
    private Integer age;

    @Column(name = "passport_number", length = 20)
    private String passportNumber;
}
