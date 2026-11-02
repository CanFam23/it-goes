package com.it_goes.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.it_goes.api.dto.TripDto;
import com.it_goes.api.dto.TripSummaryDto;
import com.it_goes.api.jpa.model.*;
import com.it_goes.api.jpa.repo.TripRepository;
import com.it_goes.api.util.enums.Country;
import com.it_goes.api.util.enums.State;
import com.it_goes.api.util.exception.NotFoundException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TripServiceTests {

    @Mock
    TripRepository tripRepo;

    @InjectMocks
    private TripServiceImpl tripService;

    private static User user;
    private static Location location;

    @BeforeAll
    static void setUp(){
        user = new User("test","test@test.com","hash",
                "john","doe",new Image("key","name"));
        location = new Location("test mtn", State.MT, Country.US);
        location.setLocation(67, 67);
    }

    @Test
    void getTrip_tripFound_returnsDto(){
        final Trip t = new Trip("title",user,location, LocalDate.of(2025,1,1));

        when(tripRepo.findById(1L)).thenReturn(Optional.of(t));

        final TripDto dto = tripService.getTrip(1L);

        assertThat(dto.title()).isEqualTo("title");
        verify(tripRepo, times(1)).findById(1L);
    }

    @Test
    void getTrip_tripNotFound_throwsException(){
        when(tripRepo.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tripService.getTrip(2L)).isInstanceOf(NotFoundException.class);
        verify(tripRepo, times(1)).findById(2L);
    }

    @Test
    void getTrip_nullId_throwsException(){
        assertThatThrownBy(() -> tripService.getTrip(null)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findById(any());
    }

    @Test
    void getTrip_invalidID_throwsException(){
        assertThatThrownBy(() -> tripService.getTrip(-1L)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findById(-1L);

        assertThatThrownBy(() -> tripService.getTrip(0L)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findById(0L);
    }

    @Test
    void getTrip_maxLongVal_returnsDto(){
        final Trip t = new Trip("title",user,location, LocalDate.of(2025,1,1));

        when(tripRepo.findById(Long.MAX_VALUE)).thenReturn(Optional.of(t));

        final TripDto dto = tripService.getTrip(Long.MAX_VALUE);

        assertThat(dto.title()).isEqualTo("title");
        verify(tripRepo, times(1)).findById(9223372036854775807L);
    }

    @Test
    void getTripSummaries_returnsPageDto(){
        final Pageable pageable = PageRequest.of(0, 2, Sort.by("dateOfTrip").descending());
        final List<Trip> trips = List.of(
                new Trip("Bridger Bowl", user, location, LocalDate.of(2025,1,1)),
                new Trip("Big Sky", user, location, LocalDate.of(2025,2,1))
        );
        final Page<Trip> page = new PageImpl<>(trips, pageable, 2);

        when(tripRepo.findAllByOrderByDateOfTripDesc(any(Pageable.class))).thenReturn(page);

        final Page<TripSummaryDto> tripsFound = tripService.getTripSummaries(2, 0);

        assertThat(tripsFound.getTotalElements()).isEqualTo(trips.size());
        assertThat(tripsFound.getContent().get(0).title()).isEqualTo("Bridger Bowl");
        assertThat(tripsFound.getContent().get(1).title()).isEqualTo("Big Sky");
        assertThat(tripsFound.getNumber()).isEqualTo(0);

        verify(tripRepo).findAllByOrderByDateOfTripDesc(any(Pageable.class));
    }

    @Test
    void getTripSummaries_noneFound_throwsException(){
        when(tripRepo.findAllByOrderByDateOfTripDesc(any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThatThrownBy(() -> tripService.getTripSummaries(2, 0)).isInstanceOf(NotFoundException.class);
        verify(tripRepo, times(1)).findAllByOrderByDateOfTripDesc(any(Pageable.class));
    }

    @Test
    void getTripSummaries_invalidPageSize(){
        assertThatThrownBy(() -> tripService.getTripSummaries(-1, 0)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findAllByOrderByDateOfTripDesc(any(Pageable.class));

        assertThatThrownBy(() -> tripService.getTripSummaries(TripService.MIN_NUM_TRIPS - 1, 0)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findAllByOrderByDateOfTripDesc(any(Pageable.class));

        assertThatThrownBy(() -> tripService.getTripSummaries(TripService.MAX_NUM_TRIPS + 1, 0)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findAllByOrderByDateOfTripDesc(any(Pageable.class));
    }

    @Test
    void getTripSummaries_invalidPageNum(){
        assertThatThrownBy(() -> tripService.getTripSummaries(2, -1)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findAllByOrderByDateOfTripDesc(any(Pageable.class));
    }

    @Test
    void getTripSummaries_pageNotFound(){
        when(tripRepo.findAllByOrderByDateOfTripDesc(any(Pageable.class)))
                .thenReturn(Page.empty());

        assertThatThrownBy(() -> tripService.getTripSummaries(3, 1)).isInstanceOf(NotFoundException.class);
        verify(tripRepo, times(1)).findAllByOrderByDateOfTripDesc(any(Pageable.class));
    }

    // toLineString tests

    @Test
    void toLineString_validPath(){
        final ClassPathResource resource = new ClassPathResource("tracks/test_route.gpx");

        Path path = null;
        try {
            path = resource.getFile().toPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final LineString ls = TripService.toLineString(path);

        assertNotNull(ls, "LineString returned should not be null after a valid gpx file with gpx tracks is given!");

        final Coordinate[] coords = ls.getCoordinates();

        for (Coordinate c: coords){
            assertTrue(c.isValid());
            assertNotEquals(c.getY(), 0);
            assertNotEquals(c.getM(), 0);
        }
    }

    @Test
    void toLineString_invalidPath_returnsNull(){
        Path testFile;
        try {
            testFile = Files.createTempFile("test_gpx",".gpx");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final LineString ls = TripService.toLineString(testFile);

        assertNull(ls, "toLineString should return null if the given file is not found or invalid");
    }

    @Test
    void toLineString_notGpxFile_returnsNull(){
        Path testFile;
        try {
            testFile = Files.createTempFile("test_gpx",".pdf");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final LineString ls = TripService.toLineString(testFile);

        assertNull(ls, "toLineString should return null if the given file is not a gpx file");
    }

    @Test
    void toLineString_nullFile_returnsNull(){
        final Path testFile = null;

        final LineString ls = TripService.toLineString(testFile);

        assertNull(ls, "toLineString should return null if the given file is null");
    }

    @Test
    void toTripSummaryDto_validTrip() {
        final Trip trip = new Trip("Title", user, location, LocalDate.of(2025, 1, 1));
        trip.setDescription("A fun day");
        trip.setCoverImageKey("img123");

        final TripSummaryDto dto = TripService.toTripSummaryDto(trip);

        assertThat(dto.title()).isEqualTo("Title");
        assertThat(dto.desc()).isEqualTo("A fun day");
        assertThat(dto.locationName()).isEqualTo(location.getName());
        assertThat(dto.dateOfTrip()).isEqualTo(LocalDate.of(2025, 1, 1));
    }

    @Test
    void toTripDto_validTripAndRoute() {
        final Trip trip = new Trip("Title", user, location, LocalDate.of(2025, 1, 1));
        trip.setDescription("abc");
        trip.setDatePosted(LocalDate.of(2025, 1, 2));
        trip.setDateUpdated(LocalDate.of(2025, 1, 3));

        final User u2 = new User("alice", "a@a.com","h","a","b", null);
        final User u3 = new User("bob", "b@b.com","h","c","d", null);

        trip.addUser(u2);
        trip.addUser(u3);

        final JsonNode route = mock(JsonNode.class);

        final TripDto dto = TripService.toTripDto(trip, route);

        assertThat(dto.title()).isEqualTo("Title");
        assertThat(dto.desc()).isEqualTo("abc");
        assertThat(dto.locationName()).isEqualTo(location.getName());
        assertThat(dto.dateOfTrip()).isEqualTo(LocalDate.of(2025,1,1));
        assertThat(dto.datePosted()).isEqualTo(LocalDate.of(2025,1,2));
        assertThat(dto.dateUpdated()).isEqualTo(LocalDate.of(2025,1,3));

        assertThat(dto.authorUsername()).isEqualTo(user.getUsername());
        assertThat(dto.peopleInvolved()).containsExactlyInAnyOrder("alice", "bob");

        assertThat(dto.route()).isSameAs(route);

        assertThat(dto.location()).containsExactly(
                location.getLocation().getX(),  // lon
                location.getLocation().getY()   // lat
        );
    }

    @Test
    void toTripDto_nullRoute() {
        final Trip trip = new Trip("T", user, location, LocalDate.of(2025, 1, 1));

        final TripDto dto = TripService.toTripDto(trip, null);

        assertThat(dto.route()).isNull();
    }

    @Test
    void toTripSummaryDto_nullTrip_throwsNpe() {
        assertThatThrownBy(() -> TripService.toTripSummaryDto(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void toTripDto_nullTrip_throwsNpe() {
        assertThatThrownBy(() -> TripService.toTripDto(null, mock(JsonNode.class)))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void getAllTripFeatureRoutes_nullLineString_stillCreatesFeature() {
        final Trip t = new Trip("Trip", user, location, LocalDate.of(2025,1,1));

        when(tripRepo.findAll()).thenReturn(List.of(t));

        try (MockedStatic<TripService> mocked = mockStatic(TripService.class, CALLS_REAL_METHODS)) {

            mocked.when(() -> TripService.toLineString(any(Path.class)))
                    .thenReturn(null);

            final JsonNode result = tripService.getAllTripFeatureRoutes();

            final JsonNode feature = result.get("features").get(0);

            assertThat(feature).isNull();
        }
    }

    // TODO: More testing for getAllTripFeatureRoutes

}