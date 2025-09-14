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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
        verify(tripRepo, times(1)).findById(2L); // ensure repo was called
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
        verify(tripRepo, times(1)).findById(9223372036854775807L); // ensure repo was called
    }

    @Test
    void getTripSummaries_returnsPageDto(){
        final Pageable pageable = PageRequest.of(0,2, Sort.by("dateOfTrip").descending());
        final List<Trip> trips = List.of(
                new Trip("Bridger Bowl", user, location, LocalDate.of(2025,1,1)),
                new Trip("Big Sky", user, location, LocalDate.of(2025,2,1))
        );
        final Page<Trip> page = new PageImpl<>(trips, pageable,2);

        when(tripRepo.findAllByOrderByDateOfTripDesc(any(Pageable.class))).thenReturn(page);

        final Page<TripSummaryDto> tripsFound = tripService.getTripSummaries(2,0);

        assertThat(tripsFound.getTotalElements()).isEqualTo(trips.size());

        assertThat(tripsFound.getContent().get(0).title()).isEqualTo("Bridger Bowl");

        assertThat(tripsFound.getContent().get(1).title()).isEqualTo("Big Sky");

        assertThat(tripsFound.getNumber()).isEqualTo(0);

        verify(tripRepo).findAllByOrderByDateOfTripDesc(any(Pageable.class));
    }

    @Test
    void getTripSummaries_noneFound_throwsException(){
        assertThatThrownBy(() -> tripService.getTripSummaries(2,0)).isInstanceOf(NotFoundException.class);
        verify(tripRepo, times(1)).findAllByOrderByDateOfTripDesc(any(Pageable.class));
    }

    @Test
    void getTripSummaries_invalidNumTrips(){
        // Shouldn't be able to get -1 trips
        assertThatThrownBy(() -> tripService.getTripSummaries(-1,0)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findAllByOrderByDateOfTripDesc(any(Pageable.class));

        assertThatThrownBy(() -> tripService.getTripSummaries(TripService.MIN_NUM_TRIPS-1,0)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findAllByOrderByDateOfTripDesc(any(Pageable.class));

        assertThatThrownBy(() -> tripService.getTripSummaries(TripService.MAX_NUM_TRIPS+1,0)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findAllByOrderByDateOfTripDesc(any(Pageable.class));
    }

    @Test
    void getTripSummaries_invalidPageNum(){
        assertThatThrownBy(() -> tripService.getTripSummaries(-1,-1)).isInstanceOf(IllegalArgumentException.class);
        verify(tripRepo, never()).findAllByOrderByDateOfTripDesc(any(Pageable.class));
    }

    @Test
    void getTripSummaries_PageNotFound(){
        when(tripRepo.findAllByOrderByDateOfTripDesc(any(Pageable.class)))
                .thenReturn(Page.empty());
        assertThatThrownBy(() -> tripService.getTripSummaries(3,1)).isInstanceOf(NotFoundException.class);
        verify(tripRepo, times(1)).findAllByOrderByDateOfTripDesc(any(Pageable.class));
    }
}
