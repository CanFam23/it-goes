package com.it_goes.api.service;

import com.it_goes.api.jpa.model.Season;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.projection.FirstNameDaysYear;
import com.it_goes.api.jpa.repo.SeasonRepository;
import com.it_goes.api.jpa.repo.UserRepository;
import com.it_goes.api.util.exception.NotFoundException;
import org.junit.jupiter.api.BeforeAll;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    private static FirstNameDaysYear fd(String firstName, int days, int year) {
        return new FirstNameDaysYear() {
            @Override public String getFirstName() { return firstName; }
            @Override public int getDaysSkied() { return days; }
            @Override public Integer getYear() { return year; }
        };
    }

    @Mock
    UserRepository userRepo;

    @Mock
    SeasonRepository seasonRepo;

    @InjectMocks
    private UserServiceImpl userService;

    private static User user;

    @BeforeAll
    static void setup(){
        user = new User("testUser","test@test.com","secret","test","test",null);
        // Set id of user (Usually it would be auto generated
        ReflectionTestUtils.setField(user, "id", 1L);
    }


    @Test
    void getUser_ID_returnsUser(){
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        final User userFound = userService.getUser(1L);

        assertThat(userFound).isEqualTo(user);
        verify(userRepo, times(1)).findById(1L); // ensure repo was called
    }

    @Test
    void getUser_unknownID_throwsException(){
        when(userRepo.findById(2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(2L)).isInstanceOf(NotFoundException.class);
        verify(userRepo, times(1)).findById(2L); // ensure repo was called
    }

    @Test
    void getUser_invalidID_throwsException(){
        assertThatThrownBy(() -> userService.getUser(-1L)).isInstanceOf(IllegalArgumentException.class);
        verify(userRepo, never()).findById(-1L); // ensure repo was never called
    }

    @Test
    void getUser_nullID_throwsException(){
        assertThatThrownBy(() -> userService.getUser(null)).isInstanceOf(IllegalArgumentException.class);
        verify(userRepo, never()).findById(-1L); // ensure repo was never called
    }

    @Test
    void getUserByUsername_returnsUser(){
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        final User userFound = userService.getUserByUsername("testUser");
        assertThat(userFound).isEqualTo(user);
        verify(userRepo, times(1)).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_whitespaces_returnsUser(){
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        final User userFound = userService.getUserByUsername("     testUser     ");
        assertThat(userFound).isEqualTo(user);
        verify(userRepo, times(1)).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_someCapitalLetters_returnsUser(){
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        final User userFound = userService.getUserByUsername("teStUSer");
        assertThat(userFound).isEqualTo(user);
        verify(userRepo, times(1)).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_allCapitalLetters_returnsUser(){
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        final User userFound = userService.getUserByUsername("TESTUSER");
        assertThat(userFound).isEqualTo(user);
        verify(userRepo, times(1)).findByUsername("testuser");
    }

    @Test
    void getUserByUsername_userNotFound(){
        when(userRepo.findByUsername("test")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserByUsername("test")).isInstanceOf(NotFoundException.class);
        verify(userRepo, times(1)).findByUsername("test");
    }

    @Test
    void getUserByUsername_spacesInName_userNotFound(){
        assertThatThrownBy(() -> userService.getUserByUsername("te st")).isInstanceOf(IllegalArgumentException.class);
        verify(userRepo, never()).findByUsername("te st");
    }

    @Test
    void getUserByUsername_shortUsername_userNotFound(){
        assertThatThrownBy(() -> userService.getUserByUsername("st")).isInstanceOf(IllegalArgumentException.class);
        verify(userRepo, never()).findByUsername("st");
    }

    @Test
    void getUserByUsername_longUsername_userNotFound(){
        assertThatThrownBy(() -> userService.getUserByUsername("s".repeat(50))).isInstanceOf(IllegalArgumentException.class);
        verify(userRepo, never()).findByUsername("s".repeat(50));
    }

    @Test
    void getUserByUsername_allInvalidChars_userNotFound(){
        assertThatThrownBy(() -> userService.getUserByUsername(",./!@#$%^&*()")).isInstanceOf(IllegalArgumentException.class);
        verify(userRepo, never()).findByUsername(",./!@#$%^&*()");
    }

    @Test
    void getUserByUsername_someInvalidChars_userNotFound(){
        assertThatThrownBy(() -> userService.getUserByUsername("testUser!")).isInstanceOf(IllegalArgumentException.class);
        verify(userRepo, never()).findByUsername("testUser!");
    }

    /**
     * Happy path: repo returns two projections; ensure pass through and order are correct.
     */
    @Test
    void getDaysSkied_validYear_returnsList() {
        final int year = 2024;
        final Season season = new Season(year);
        final List<FirstNameDaysYear> mockResult = List.of(
                fd("Alice", 12, year),
                fd("Bob", 7, year)
        );
        when(userRepo.getDaysSkied(season.getStartDate(),season.getEndDate())).thenReturn(mockResult);
        when(seasonRepo.findByStartYear(year)).thenReturn(Optional.of(season));

        final List<FirstNameDaysYear> result = userService.getDaysSkied(year);

        assertThat(result)
                .extracting(FirstNameDaysYear::getFirstName, FirstNameDaysYear::getDaysSkied, FirstNameDaysYear::getYear)
                .containsExactly(
                        tuple("Alice", 12, year),
                        tuple("Bob", 7, year)
                );
        verify(userRepo, times(1)).getDaysSkied(season.getStartDate(),season.getEndDate());
    }

    @Test
    void getDaysSkied_nullYear_returnsList() {
        final int year = 2024;
        final Season season = new Season(year);
        final List<FirstNameDaysYear> mockResult = List.of(
                fd("Alice", 12, year),
                fd("Bob", 7, year)
        );
        when(userRepo.getDaysSkied()).thenReturn(mockResult);

        final List<FirstNameDaysYear> result = userService.getDaysSkied(null);

        assertThat(result)
                .extracting(FirstNameDaysYear::getFirstName, FirstNameDaysYear::getDaysSkied, FirstNameDaysYear::getYear)
                .containsExactly(
                        tuple("Alice", 12, year),
                        tuple("Bob", 7, year)
                );
        verify(userRepo, times(1)).getDaysSkied();
    }

    /**
     * Repo returns empty list; ensure service returns empty list too.
     */
    @Test
    void getDaysSkied_validYear_noData_returnsEmptyList() {
        final int year = 2023;
        final Season season = new Season(year);
        when(userRepo.getDaysSkied(season.getStartDate(),season.getEndDate())).thenReturn(List.of());
        when(seasonRepo.findByStartYear(year)).thenReturn(Optional.of(season));

        final List<FirstNameDaysYear> result = userService.getDaysSkied(year);

        assertThat(result).isEmpty();
        verify(userRepo, times(1)).getDaysSkied(season.getStartDate(),season.getEndDate());
    }

    /**
     * Invalid year (zero or negative) should be rejected by the service.
     */
    @Test
    void getDaysSkied_invalidYear_zero_throws() {
        assertThatThrownBy(() -> userService.getDaysSkied(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Unreasonably old season start year should be rejected.
     */
    @Test
    void getDaysSkied_invalidYear_tooEarly_throws() {
        assertThatThrownBy(() -> userService.getDaysSkied(1800))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Clearly far-future year should be rejected (guard against typos).
     */
    @Test
    void getDaysSkied_invalidYear_farFuture_throws() {
        assertThatThrownBy(() -> userService.getDaysSkied(9999))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
