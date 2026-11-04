package com.it_goes.api.service;

import com.it_goes.api.jpa.model.Location;
import com.it_goes.api.jpa.model.SkiDayLog;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.repo.LocationRepository;
import com.it_goes.api.jpa.repo.SkiDayLogRepository;
import com.it_goes.api.jpa.repo.UserRepository;
import com.it_goes.api.util.exception.AccountRequestException;
import com.it_goes.api.util.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class SkiDayService {
    private final SkiDayLogRepository logs;
    private final UserRepository users;
    private final LocationRepository locations;

    public SkiDayService(SkiDayLogRepository logs, UserRepository users, LocationRepository locations) {
        this.logs = logs;
        this.users = users;
        this.locations = locations;
    }

    @Transactional(readOnly = true)
    public DayList list(String username) {
        User user = user(username);
        LocalDate today = LocalDate.now();
        int year = today.getMonthValue() >= 10 ? today.getYear() : today.getYear() - 1;
        LocalDate start = LocalDate.of(year, 10, 1);
        List<DayEntry> entries = logs.findByUserOrderByDateDescIdDesc(user).stream().map(DayEntry::of).toList();
        return new DayList(year, logs.countDistinctDays(user.getId(), start, start.plusYears(1).minusDays(1)), entries);
    }

    @Transactional
    public DayEntry create(String username, LocalDate date, Long locationId) {
        validate(date, locationId);
        User user = user(username);
        Location location = location(locationId);
        if (logs.existsByUserAndDateAndLocation(user, date, location)) {
            throw new AccountRequestException(HttpStatus.CONFLICT, "This day is already logged at that location.");
        }
        return DayEntry.of(logs.save(new SkiDayLog(user, date, location)));
    }

    @Transactional
    public DayEntry update(String username, Long id, LocalDate date, Long locationId) {
        validate(date, locationId);
        SkiDayLog log = owned(username, id);
        Location location = location(locationId);
        if ((!log.getDate().equals(date) || !log.getLocation().getId().equals(locationId))
                && logs.existsByUserAndDateAndLocation(log.getUser(), date, location)) {
            throw new AccountRequestException(HttpStatus.CONFLICT, "This day is already logged at that location.");
        }
        log.change(date, location);
        return DayEntry.of(log);
    }

    @Transactional
    public void delete(String username, Long id) {
        logs.delete(owned(username, id));
    }

    private SkiDayLog owned(String username, Long id) {
        SkiDayLog log = logs.findById(id).orElseThrow(() -> new NotFoundException("Ski day not found."));
        if (!log.getUser().getUsername().equals(username)) {
            throw new AccountRequestException(HttpStatus.FORBIDDEN, "You cannot change another user's ski day.");
        }
        return log;
    }

    private User user(String username) {
        return users.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found."));
    }

    private Location location(Long id) {
        return locations.findById(id).orElseThrow(() -> new NotFoundException("Location not found."));
    }

    private void validate(LocalDate date, Long locationId) {
        if (date == null || locationId == null || locationId < 1 || date.isAfter(LocalDate.now())) {
            throw new AccountRequestException(HttpStatus.BAD_REQUEST, "Enter a valid past or current date and location.");
        }
    }

    public record DayEntry(Long id, LocalDate date, Long locationId, String locationName) {
        static DayEntry of(SkiDayLog log) {
            return new DayEntry(log.getId(), log.getDate(), log.getLocation().getId(), log.getLocation().getName());
        }
    }
    public record DayList(int seasonStartYear, long totalDays, List<DayEntry> entries) {}
}
