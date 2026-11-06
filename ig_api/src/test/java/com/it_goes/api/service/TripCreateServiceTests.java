package com.it_goes.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.it_goes.api.dto.CreateTripRequest;
import com.it_goes.api.jpa.model.*;
import com.it_goes.api.jpa.repo.*;
import com.it_goes.api.util.enums.Country;
import com.it_goes.api.util.enums.State;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class TripCreateServiceTests {
    @Mock TripRepository trips;
    @Mock UserRepository users;
    @Mock LocationRepository locations;
    @Mock LocationService locationService;
    @Mock SeasonRepository seasons;
    @InjectMocks TripServiceImpl service;
    private final ObjectMapper mapper = new ObjectMapper();

    private JsonNode body() throws Exception {
        return mapper.readTree("""
                {"root":{"type":"root","children":[{"type":"paragraph","children":[{"type":"text","text":"Great snow","format":1}]}]}}
                """);
    }

    @Test
    void createsTripInDateDerivedSeasonAndAddsCompanions() throws Exception {
        User author = new User("alice", "a@example.com", "hash", "Alice", "A", null);
        User companion = new User("bob", "b@example.com", "hash", "Bob", "B", null);
        Location place = new Location("Beehive", State.MT, Country.US);
        ReflectionTestUtils.setField(author, "id", 1L);
        ReflectionTestUtils.setField(companion, "id", 2L);
        ReflectionTestUtils.setField(place, "id", 3L);
        when(users.findByUsername("alice")).thenReturn(Optional.of(author));
        when(users.findAllById(List.of(2L))).thenReturn(List.of(companion));
        when(locations.findById(3L)).thenReturn(Optional.of(place));
        when(seasons.findByStartYear(2024)).thenReturn(Optional.empty());
        when(seasons.save(any(Season.class))).thenAnswer(call -> call.getArgument(0));
        when(trips.saveAndFlush(any(Trip.class))).thenAnswer(call -> {
            Trip trip = call.getArgument(0);
            ReflectionTestUtils.setField(trip, "id", 7L);
            return trip;
        });

        var result = service.createTrip("alice", new CreateTripRequest("A trip", LocalDate.of(2025, 1, 10),
                3L, List.of(2L), body(), null));

        assertThat(result.id()).isEqualTo(7L);
        assertThat(result.desc()).isEqualTo("Great snow");
        assertThat(result.peopleInvolved()).containsExactly("bob");
        assertThat(result.imageURL()).isNull();
        var capture = org.mockito.ArgumentCaptor.forClass(Trip.class);
        verify(trips).saveAndFlush(capture.capture());
        assertThat(capture.getValue().getSeason().getStartDate()).isEqualTo(LocalDate.of(2024, 10, 1));
        assertThat(capture.getValue().getAuthor()).isSameAs(author);
    }

    @Test
    void rejectsUnsupportedNodesAndUnsafeLinks() throws Exception {
        assertThatThrownBy(() -> TripBody.validateAndSummarize(mapper.readTree("""
                {"root":{"type":"root","children":[{"type":"image","src":"x","children":[]}]}}
                """))).hasMessageContaining("valid formatted trip body");
        assertThatThrownBy(() -> TripBody.validateAndSummarize(mapper.readTree("""
                {"root":{"type":"root","children":[{"type":"paragraph","children":[{"type":"link","url":"javascript:alert(1)","children":[{"type":"text","text":"bad"}]}]}]}}
                """))).hasMessageContaining("valid formatted trip body");
    }

    @Test
    void newLocationIsCreatedInsideTripServiceTransaction() throws Exception {
        User author = new User("alice", "a@example.com", "hash", "Alice", "A", null);
        Location place = new Location("Chamonix", null, Country.FR);
        ReflectionTestUtils.setField(author, "id", 1L);
        ReflectionTestUtils.setField(place, "id", 8L);
        when(users.findByUsername("alice")).thenReturn(Optional.of(author));
        when(locationService.create("Chamonix", Country.FR, null)).thenReturn(place);
        when(users.findAllById(List.of())).thenReturn(List.of());
        when(seasons.findByStartYear(2024)).thenReturn(Optional.of(new Season(2024)));
        when(trips.saveAndFlush(any(Trip.class))).thenAnswer(call -> {
            Trip trip = call.getArgument(0);
            ReflectionTestUtils.setField(trip, "id", 7L);
            return trip;
        });

        var result = service.createTrip("alice", new CreateTripRequest("A trip", LocalDate.of(2025, 1, 10),
                null, new CreateTripRequest.NewLocation("Chamonix", Country.FR, null), List.of(), body(), null));

        assertThat(result.locationName()).isEqualTo("Chamonix");
        verify(locationService).create("Chamonix", Country.FR, null);
        verify(locations, never()).findById(anyLong());
    }

    @Test
    void exactlyOneLocationSourceIsRequired() throws Exception {
        var date = LocalDate.of(2025, 1, 10);
        var newLocation = new CreateTripRequest.NewLocation("Chamonix", Country.FR, null);
        assertThatThrownBy(() -> service.createTrip("alice", new CreateTripRequest("A trip", date,
                null, null, List.of(), body(), null))).hasMessageContaining("existing location or add a new one");
        assertThatThrownBy(() -> service.createTrip("alice", new CreateTripRequest("A trip", date,
                3L, newLocation, List.of(), body(), null))).hasMessageContaining("existing location or add a new one");
        verifyNoInteractions(trips, locationService);
    }
}
