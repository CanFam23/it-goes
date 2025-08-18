package com.it_goes.api.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TripServiceTests {

    @Mock
    TripRepository tripRepo;

    @InjectMocks
    private TripService tripService;

    private static User user;
    private static Location location;

    @BeforeAll
    static void setUp(){
        user = new User("test","test@test.com","hash",
                "john","doe",new Image("key","url","name"));
        location = new Location("test mtn", State.MT, Country.US);
    }

    @Test
    void getTrip_userFound_returnsDto(){
        final Trip t = new Trip("title",user,location, LocalDate.of(2025,1,1));

        when(tripRepo.findById(1L)).thenReturn(Optional.of(t));

        final TripDto dto = tripService.getTrip(1L);

        assertThat(dto.title()).isEqualTo("title");
        verify(tripRepo, times(1)).findById(1L); // ensure repo was called
    }

    @Test
    void getTrip_noUserFound_throwsException(){
        when(tripRepo.findById(2L)).thenReturn(Optional.empty());

        // Verify exception thrown when trip with given id isn't found
        assertThatThrownBy(() -> tripService.getTrip(2L)).isInstanceOf(NotFoundException.class);
        verify(tripRepo, times(1)).findById(1L); // ensure repo was called
    }

    @Test
    void getTrip_nullUser_throwsException(){
        // Verify exception thrown when null is given
        assertThatThrownBy(() -> tripService.getTrip(null)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findById(1L);
    }

    @Test
    void getTrip_invalidID_throwsException(){
        // Verify exception thrown when -1L is given
        assertThatThrownBy(() -> tripService.getTrip(-1L)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findById(-1L);
    }

    @Test
    void getTrip_maxLongVal_returnsDto(){
        final Trip t = new Trip("title",user,location, LocalDate.of(2025,1,1));

        when(tripRepo.findById(Long.MAX_VALUE)).thenReturn(Optional.of(t));

        final TripDto dto = tripService.getTrip(Long.MAX_VALUE);

        // Verify trip is found
        assertThat(dto.title()).isEqualTo("title");
        verify(tripRepo, times(1)).findById(1L); // ensure repo was called
    }

    @Test
    void getRecentTripSummaries_returnsPageDto(){
        final Pageable pageable = PageRequest.of(1, 5, Sort.by("createdAt").descending());
        final List<Trip> trips = List.of(
                new Trip("Bridger Bowl", user, location, LocalDate.of(2025,1,1)),
                new Trip("Big Sky", user, location, LocalDate.of(2025,2,1))
        );
        final Page<Trip> page = new PageImpl<>(trips, pageable,23);

        when(tripRepo.findAll(pageable)).thenReturn(page);

        final Page<TripSummaryDto> tripsFound = tripService.getRecentTripSummaries(2);

        assertThat(tripsFound.getTotalElements()).isEqualTo(trips.size());

        assertThat(tripsFound.getContent().get(0).title()).isEqualTo("Keystone");

        assertThat(tripsFound.getNumber()).isEqualTo(1);
        assertThat(tripsFound.getSize()).isEqualTo(5);
        assertThat(tripsFound.getTotalElements()).isEqualTo(23);
        assertThat(tripsFound.getTotalPages()).isEqualTo((int) Math.ceil(23 / 5.0));
        assertThat(tripsFound.hasNext()).isTrue();

        verify(tripRepo).findAll(pageable);
    }

    @Test
    void getRecentTripSummaries_noneFound(){
        final Pageable pageable = PageRequest.of(1, 5, Sort.by("createdAt").descending());

        when(tripRepo.findAll(pageable)).thenReturn(Page.empty(pageable));

        final Page<TripSummaryDto> tripsFound = tripService.getRecentTripSummaries(2);

        assertThat(tripsFound).isNotNull();
        assertThat(tripsFound.hasContent()).isFalse();
        assertThat(tripsFound.getContent()).isEmpty();
        assertThat(tripsFound.getTotalElements()).isZero();
        assertThat(tripsFound.getTotalPages()).isZero();
        assertThat(tripsFound.getNumber()).isEqualTo(0);
        assertThat(tripsFound.getSize()).isEqualTo(10);
    }

    @Test
    void getRecentTripSummaries_invalidParameter(){
        // Shouldn't be able to get -1 trips
        assertThatThrownBy(() -> tripService.getRecentTripSummaries(-1)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findAllByOrderByDateOfTripDesc(any(Pageable.class));

        assertThatThrownBy(() -> tripService.getRecentTripSummaries(TripService.MIN_NUM_TRIPS+1)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findAllByOrderByDateOfTripDesc(any(Pageable.class));

        assertThatThrownBy(() -> tripService.getRecentTripSummaries(TripService.MIN_NUM_TRIPS-1)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findAllByOrderByDateOfTripDesc(any(Pageable.class));
    }
}
