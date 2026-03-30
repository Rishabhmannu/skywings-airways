package com.skywings.entity;

import com.skywings.entity.enums.SeatClass;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "seats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"flight_id", "seat_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Flight flight;

    @Column(name = "seat_number", nullable = false, length = 5)
    private String seatNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private SeatClass seatClass;

    @Column(name = "is_available", nullable = false)
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(nullable = false)
    private BigDecimal price;
}
