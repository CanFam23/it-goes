package com.it_goes.api.jpa.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "ski_day_log", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "ski_date", "location_id"}))
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class SkiDayLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "ski_date", nullable = false)
    private LocalDate date;

    public SkiDayLog(User user, LocalDate date, Location location) {
        this.user = user;
        this.date = date;
        this.location = location;
    }

    public void change(LocalDate date, Location location) {
        this.date = date;
        this.location = location;
    }
}
