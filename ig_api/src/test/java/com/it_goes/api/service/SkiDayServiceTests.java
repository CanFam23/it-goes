package com.it_goes.api.service;

import com.it_goes.api.jpa.model.Location;
import com.it_goes.api.jpa.model.SkiDayLog;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.repo.LocationRepository;
import com.it_goes.api.jpa.repo.SkiDayLogRepository;
import com.it_goes.api.jpa.repo.UserRepository;
import com.it_goes.api.util.enums.Country;
import com.it_goes.api.util.enums.State;
import com.it_goes.api.util.exception.AccountRequestException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SkiDayServiceTests {
    private final SkiDayLogRepository logs = mock(SkiDayLogRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final LocationRepository locations = mock(LocationRepository.class);
    private final SkiDayService service = new SkiDayService(logs, users, locations);
    private final User owner = new User("owner", "owner@example.com", "hash", "Day", "Skier", null);
    private final Location location = new Location("Beehive", State.MT, Country.US);

    @Test
    void createRejectsAnExistingUserDateLocation() {
        LocalDate date = LocalDate.of(2025, 1, 10);
        when(users.findByUsername("owner")).thenReturn(Optional.of(owner));
        when(locations.findById(2L)).thenReturn(Optional.of(location));
        when(logs.existsByUserAndDateAndLocation(owner, date, location)).thenReturn(true);

        assertThatThrownBy(() -> service.create("owner", date, 2L))
                .isInstanceOf(AccountRequestException.class)
                .hasMessageContaining("already logged");
        verify(logs, never()).save(any());
    }

    @Test
    void anotherUserCannotEditAnEntry() {
        SkiDayLog entry = new SkiDayLog(owner, LocalDate.of(2025, 1, 10), location);
        when(logs.findById(9L)).thenReturn(Optional.of(entry));

        assertThatThrownBy(() -> service.update("stranger", 9L, LocalDate.of(2025, 1, 11), 2L))
                .isInstanceOf(AccountRequestException.class);
        verify(logs, never()).save(any());
    }
}
