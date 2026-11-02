package com.it_goes.api.service;

import com.it_goes.api.dto.UserDto;
import com.it_goes.api.jpa.model.AccessCode;
import com.it_goes.api.jpa.model.Season;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.projection.FirstNameDaysLocationYear;
import com.it_goes.api.jpa.projection.FirstNameDaysYear;
import com.it_goes.api.jpa.repo.AccessCodeRepository;
import com.it_goes.api.jpa.repo.SeasonRepository;
import com.it_goes.api.jpa.repo.UserRepository;
import com.it_goes.api.util.exception.NotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeAll;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
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

    private static FirstNameDaysLocationYear fdl(String firstName, String location, int days, int year) {
        return new FirstNameDaysLocationYear() {
            public String getFirstName() { return firstName; }
            public String getLocation() { return location; }
            public int getDaysSkied() { return days; }
            public Integer getYear() { return year; }
        };
    }

    @Mock
    UserRepository userRepo;

    @Mock
    SeasonRepository seasonRepo;

    @Mock
    AccessCodeRepository accessCodeRepo;

    @Mock
    PasswordEncoder passwordEncoder;

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

    /**
     * Happy path: repo returns two projections for different locations; ensure pass through and order are correct.
     */
    @Test
    void getDaysSkiedEachLocation_validYear_returnsList() {
        final int year = 2024;
        final Season season = new Season(year);
        final List<FirstNameDaysLocationYear> mockResult = List.of(
                fdl("Alice", "Aspen", 5, year),
                fdl("Bob", "Vail", 3, year)
        );
        when(userRepo.getDaysSkiedEachLocation(season.getStartDate(), season.getEndDate()))
                .thenReturn(mockResult);
        when(seasonRepo.findByStartYear(year)).thenReturn(Optional.of(season));

        final List<FirstNameDaysLocationYear> result = userService.getDaysSkiedEachLocation(year);

        assertThat(result)
                .extracting(FirstNameDaysLocationYear::getFirstName,
                        FirstNameDaysLocationYear::getLocation,
                        FirstNameDaysLocationYear::getDaysSkied,
                        FirstNameDaysLocationYear::getYear)
                .containsExactly(
                        tuple("Alice", "Aspen", 5, year),
                        tuple("Bob", "Vail", 3, year)
                );
        verify(userRepo, times(1))
                .getDaysSkiedEachLocation(season.getStartDate(), season.getEndDate());
    }

    /**
     * Null year: repo method without date filter should be used.
     */
    @Test
    void getDaysSkiedEachLocation_nullYear_returnsList() {
        final int year = 2024;
        final List<FirstNameDaysLocationYear> mockResult = List.of(
                fdl("Alice", "Aspen", 5, year),
                fdl("Bob", "Vail", 3, year)
        );
        when(userRepo.getDaysSkiedEachLocation()).thenReturn(mockResult);

        final List<FirstNameDaysLocationYear> result = userService.getDaysSkiedEachLocation(null);

        assertThat(result)
                .extracting(FirstNameDaysLocationYear::getFirstName,
                        FirstNameDaysLocationYear::getLocation,
                        FirstNameDaysLocationYear::getDaysSkied,
                        FirstNameDaysLocationYear::getYear)
                .containsExactly(
                        tuple("Alice", "Aspen", 5, year),
                        tuple("Bob", "Vail", 3, year)
                );
        verify(userRepo, times(1)).getDaysSkiedEachLocation();
    }

    /**
     * Repo returns empty list; ensure service returns empty list too.
     */
    @Test
    void getDaysSkiedEachLocation_validYear_noData_returnsEmptyList() {
        final int year = 2023;
        final Season season = new Season(year);
        when(userRepo.getDaysSkiedEachLocation(season.getStartDate(), season.getEndDate()))
                .thenReturn(List.of());
        when(seasonRepo.findByStartYear(year)).thenReturn(Optional.of(season));

        final List<FirstNameDaysLocationYear> result = userService.getDaysSkiedEachLocation(year);

        assertThat(result).isEmpty();
        verify(userRepo, times(1))
                .getDaysSkiedEachLocation(season.getStartDate(), season.getEndDate());
    }

    /**
     * Invalid year (zero or negative) should be rejected by the service.
     */
    @Test
    void getDaysSkiedEachLocation_invalidYear_zero_throws() {
        assertThatThrownBy(() -> userService.getDaysSkiedEachLocation(0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Unreasonably old season start year should be rejected.
     */
    @Test
    void getDaysSkiedEachLocation_invalidYear_tooEarly_throws() {
        assertThatThrownBy(() -> userService.getDaysSkiedEachLocation(1800))
                .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Clearly far-future year should be rejected (guard against typos).
     */
    @Test
    void getDaysSkiedEachLocation_invalidYear_farFuture_throws() {
        assertThatThrownBy(() -> userService.getDaysSkiedEachLocation(9999))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createUser_usernameExists_returnsEmpty() {
        UserDto dto = new UserDto("validUser", "email@test.com", "Valid123!", "fn", "ln", null);

        when(userRepo.existsByUsername("validUser")).thenReturn(true);

        Optional<User> result = userService.createUser(dto);

        assertThat(result).isEmpty();
        verify(userRepo, never()).save(any());
        verify(userRepo).existsByUsername("validUser");
        verify(userRepo, never()).existsByEmail(any());
    }

    @Test
    void createUser_emailExists_returnsEmpty() {
        UserDto dto = new UserDto("validUser", "email@test.com", "Valid123!", "fn", "ln", null);

        when(userRepo.existsByUsername("validUser")).thenReturn(false);
        when(userRepo.existsByEmail("email@test.com")).thenReturn(true);

        Optional<User> result = userService.createUser(dto);

        assertThat(result).isEmpty();
        verify(userRepo, never()).save(any());
        verify(userRepo).existsByUsername("validUser");
        verify(userRepo).existsByEmail("email@test.com");
    }

    @Test
    void createUser_validDto_savesAndReturnsUser() {
        UserDto dto = new UserDto("validUser", "email@test.com", "Valid123!", "fn", "ln", null);

        User saved = new User("validUser","email@test.com","ENCODED","fn","ln",null);
        ReflectionTestUtils.setField(saved, "id", 10L);

        when(userRepo.existsByUsername("validUser")).thenReturn(false);
        when(userRepo.existsByEmail("email@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Valid123!")).thenReturn("ENCODED");
        when(userRepo.save(any(User.class))).thenReturn(saved);

        Optional<User> result = userService.createUser(dto);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(10L);

        verify(userRepo).existsByUsername("validUser");
        verify(userRepo).existsByEmail("email@test.com");
        verify(userRepo).save(any(User.class));
    }

    @Test
    void createUser_repoThrows_returnsEmpty() {
        UserDto dto = new UserDto("validUser", "email@test.com", "Valid123!", "fn", "ln", null);

        when(userRepo.existsByUsername("validUser")).thenReturn(false);
        when(userRepo.existsByEmail("email@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Valid123!")).thenReturn("ENCODED");
        when(userRepo.save(any(User.class))).thenThrow(new IllegalArgumentException("fail"));

        Optional<User> result = userService.createUser(dto);

        assertThat(result).isEmpty();
    }

    @Test
    void deleteUserByEmail_validEmailNotFound_returnsFalse() {
        String email = "test@test.com";

        when(userRepo.existsByEmail(email)).thenReturn(false);

        boolean result = userService.deleteUserByEmail(email);

        assertThat(result).isFalse();
        verify(userRepo, never()).deleteUserByEmail(any());
        verify(userRepo, times(1)).existsByEmail(email);
    }


    @Test
    void deleteUserByEmail_validEmail_deletedSuccessfully_returnsTrue() {
        String email = "test@test.com";

        when(userRepo.existsByEmail(email))
                .thenReturn(true)   // before deletion
                .thenReturn(false); // after deletion

        boolean result = userService.deleteUserByEmail(email);

        assertThat(result).isTrue();
        verify(userRepo, times(1)).deleteUserByEmail(email);
        verify(userRepo, times(2)).existsByEmail(email);
    }

    @Test
    void validateAccessCode_validCodeAndEmail_returnsTrue() {
        String email = "test@test.com";
        AccessCode accessCode = new AccessCode(email);
        ReflectionTestUtils.setField(accessCode, "code", "ABC12345");

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("ABC12345", email);

        assertThat(result).isTrue();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
    }

    @Test
    void validateAccessCode_validCodeAndEmailWithWhitespace_returnsTrue() {
        String email = "test@test.com";
        AccessCode accessCode = new AccessCode(email);
        ReflectionTestUtils.setField(accessCode, "code", "ABC12345");

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("  ABC12345  ", "  test@test.com  ");

        assertThat(result).isTrue();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
    }

    @Test
    void validateAccessCode_codeNotFound_searchesByEmail_returnsTrue() {
        String email = "test@test.com";
        AccessCode accessCode = new AccessCode(email);
        ReflectionTestUtils.setField(accessCode, "code", "ABC12345");

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.empty());
        when(accessCodeRepo.findByEmail(email)).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("ABC12345", email);

        assertThat(result).isTrue();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
        verify(accessCodeRepo, times(1)).findByEmail(email);
    }

    @Test
    void validateAccessCode_codeNotFoundAndEmailNotFound_returnsFalse() {
        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.empty());
        when(accessCodeRepo.findByEmail("test@test.com")).thenReturn(Optional.empty());

        boolean result = userService.validateAccessCode("ABC12345", "test@test.com");

        assertThat(result).isFalse();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
        verify(accessCodeRepo, times(1)).findByEmail("test@test.com");
    }

    @Test
    void validateAccessCode_codeFoundButCodeMismatch_returnsFalse() {
        String email = "test@test.com";
        AccessCode accessCode = new AccessCode(email);
        ReflectionTestUtils.setField(accessCode, "code", "XYZ78901");

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("ABC12345", email);

        assertThat(result).isFalse();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
    }

    @Test
    void validateAccessCode_codeFoundButEmailMismatch_returnsFalse() {
        AccessCode accessCode = new AccessCode("wrong@test.com");
        ReflectionTestUtils.setField(accessCode, "code", "ABC12345");

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("ABC12345", "test@test.com");

        assertThat(result).isFalse();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
    }

    @Test
    void validateAccessCode_emailFoundButCodeMismatch_returnsFalse() {
        String email = "test@test.com";
        AccessCode accessCode = new AccessCode(email);
        ReflectionTestUtils.setField(accessCode, "code", "XYZ78901");

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.empty());
        when(accessCodeRepo.findByEmail(email)).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("ABC12345", email);

        assertThat(result).isFalse();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
        verify(accessCodeRepo, times(1)).findByEmail(email);
    }

    @Test
    void validateAccessCode_emailFoundButEmailMismatch_returnsFalse() {
        AccessCode accessCode = new AccessCode("wrong@test.com");
        ReflectionTestUtils.setField(accessCode, "code", "ABC12345");

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.empty());
        when(accessCodeRepo.findByEmail("test@test.com")).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("ABC12345", "test@test.com");

        assertThat(result).isFalse();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
        verify(accessCodeRepo, times(1)).findByEmail("test@test.com");
    }

    @Test
    void validateAccessCode_usedCode_returnsFalse() {
        String email = "test@test.com";
        AccessCode accessCode = new AccessCode(email);
        ReflectionTestUtils.setField(accessCode, "code", "ABC12345");
        accessCode.setUsed(); // Mark as used

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("ABC12345", email);

        assertThat(result).isFalse();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
    }

    @Test
    void validateAccessCode_expiredCode_returnsFalse() {
        String email = "test@test.com";
        AccessCode accessCode = new AccessCode(email);
        ReflectionTestUtils.setField(accessCode, "code", "ABC12345");
        ReflectionTestUtils.setField(accessCode, "expiresAt", LocalDateTime.now().minusDays(1));

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("ABC12345", email);

        assertThat(result).isFalse();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
    }

    @Test
    void validateAccessCode_codeExpiresInFuture_returnsTrue() {
        String email = "test@test.com";
        AccessCode accessCode = new AccessCode(email);
        ReflectionTestUtils.setField(accessCode, "code", "ABC12345");
        ReflectionTestUtils.setField(accessCode, "expiresAt", LocalDateTime.now().plusDays(5));

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("ABC12345", email);

        assertThat(result).isTrue();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
    }

    @Test
    void validateAccessCode_codeExpiresExactlyNow_returnsFalse() {
        String email = "test@test.com";
        AccessCode accessCode = new AccessCode(email);
        ReflectionTestUtils.setField(accessCode, "code", "ABC12345");
        ReflectionTestUtils.setField(accessCode, "expiresAt", LocalDateTime.now());

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("ABC12345", email);

        assertThat(result).isFalse();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
    }

    @Test
    void validateAccessCode_usedAndExpired_returnsFalse() {
        String email = "test@test.com";
        AccessCode accessCode = new AccessCode(email);
        ReflectionTestUtils.setField(accessCode, "code", "ABC12345");
        ReflectionTestUtils.setField(accessCode, "expiresAt", LocalDateTime.now().minusDays(1));
        accessCode.setUsed();

        when(accessCodeRepo.findByCode("ABC12345")).thenReturn(Optional.of(accessCode));

        boolean result = userService.validateAccessCode("ABC12345", email);

        assertThat(result).isFalse();
        verify(accessCodeRepo, times(1)).findByCode("ABC12345");
    }

    @Test
    void issueAccessCode_createsCodeForNewEmail() {
        when(accessCodeRepo.findByEmailForUpdate("friend@test.com")).thenReturn(Optional.empty());
        when(accessCodeRepo.save(any(AccessCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccessCode code = userService.issueAccessCode("  FRIEND@test.com  ");

        assertThat(code.getEmail()).isEqualTo("friend@test.com");
        assertThat(code.getCode()).hasSize(8);
        assertThat(code.getExpiresAt()).isAfter(LocalDateTime.now());
        verify(accessCodeRepo).save(code);
    }

    @Test
    void issueAccessCode_replacesExistingCode() {
        AccessCode existing = new AccessCode("friend@test.com");
        String oldCode = existing.getCode();
        when(accessCodeRepo.findByEmailForUpdate("friend@test.com")).thenReturn(Optional.of(existing));
        when(accessCodeRepo.save(any(AccessCode.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AccessCode code = userService.issueAccessCode("friend@test.com");

        assertThat(code.getCode()).isNotEqualTo(oldCode);
        verify(accessCodeRepo).save(existing);
        verify(accessCodeRepo).existsByCode(code.getCode());
    }

    @Test
    void issueAccessCode_registeredEmailIsRejected() {
        when(userRepo.existsByEmail("friend@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.issueAccessCode("friend@test.com"))
                .hasMessage("An account already exists for this email.");
        verify(accessCodeRepo, never()).save(any());
    }

    @Test
    void issueAccessCode_usedInviteIsRejected() {
        AccessCode code = new AccessCode("friend@test.com");
        code.setUsed();
        when(accessCodeRepo.findByEmailForUpdate("friend@test.com")).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> userService.issueAccessCode("friend@test.com"))
                .hasMessage("This email has already used an invite.");
        verify(accessCodeRepo, never()).save(any());
    }

    @Test
    void createUserWithAccessCode_registeredEmailCannotConsumeCode() {
        AccessCode code = new AccessCode("friend@test.com");
        UserDto dto = new UserDto("friend", "friend@test.com", "Valid123!", "First", "Last", null);
        when(accessCodeRepo.findByCodeForUpdate(code.getCode())).thenReturn(Optional.of(code));
        when(userRepo.existsByEmail("friend@test.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUserWithAccessCode(dto, code.getCode()))
                .hasMessage("An account already exists for this email.");
        assertThat(code.hasBeenUsed()).isFalse();
    }

    @Test
    void createUserWithAccessCode_validCodeCreatesUserAndConsumesCode() {
        AccessCode code = new AccessCode("friend@test.com");
        UserDto dto = new UserDto("friend", "friend@test.com", "Valid123!", "First", "Last", null);
        User saved = new User("friend", "friend@test.com", "ENCODED", "First", "Last", null);
        when(accessCodeRepo.findByCodeForUpdate(code.getCode())).thenReturn(Optional.of(code));
        when(passwordEncoder.encode("Valid123!")).thenReturn("ENCODED");
        when(userRepo.save(any(User.class))).thenReturn(saved);

        Optional<User> result = userService.createUserWithAccessCode(dto, code.getCode());

        assertThat(result).contains(saved);
        assertThat(code.hasBeenUsed()).isTrue();
        verify(accessCodeRepo).save(code);
    }

    @Test
    void createUserWithAccessCode_failedUserCreationKeepsCodeUsable() {
        AccessCode code = new AccessCode("friend@test.com");
        UserDto dto = new UserDto("friend", "friend@test.com", "Valid123!", "First", "Last", null);
        when(accessCodeRepo.findByCodeForUpdate(code.getCode())).thenReturn(Optional.of(code));
        when(userRepo.existsByUsername("friend")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUserWithAccessCode(dto, code.getCode()))
                .hasMessage("Username is already taken.");
        assertThat(code.hasBeenUsed()).isFalse();
        verify(accessCodeRepo, never()).save(any());
    }

    @Test
    void createUserWithAccessCode_wrongEmailCannotCreateUser() {
        AccessCode code = new AccessCode("friend@test.com");
        UserDto dto = new UserDto("other", "other@test.com", "Valid123!", "First", "Last", null);
        when(accessCodeRepo.findByCodeForUpdate(code.getCode())).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> userService.createUserWithAccessCode(dto, code.getCode()))
                .hasMessage("This access code is not valid for that email.");
        verify(userRepo, never()).save(any());
        assertThat(code.hasBeenUsed()).isFalse();
    }

    @Test
    void createUserWithAccessCode_usedCodeCannotCreateUser() {
        AccessCode code = new AccessCode("friend@test.com");
        code.setUsed();
        UserDto dto = new UserDto("friend", "friend@test.com", "Valid123!", "First", "Last", null);
        when(accessCodeRepo.findByCodeForUpdate(code.getCode())).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> userService.createUserWithAccessCode(dto, code.getCode()))
                .hasMessage("This access code has already been used.");
        verify(userRepo, never()).save(any());
    }

    @Test
    void createUserWithAccessCode_expiredCodeCannotCreateUser() {
        AccessCode code = new AccessCode("friend@test.com");
        ReflectionTestUtils.setField(code, "expiresAt", LocalDateTime.now().minusMinutes(1));
        UserDto dto = new UserDto("friend", "friend@test.com", "Valid123!", "First", "Last", null);
        when(accessCodeRepo.findByCodeForUpdate(code.getCode())).thenReturn(Optional.of(code));

        assertThatThrownBy(() -> userService.createUserWithAccessCode(dto, code.getCode()))
                .hasMessage("This access code has expired.");
        verify(userRepo, never()).save(any());
    }
}
