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
import jakarta.persistence.OptimisticLockException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Validated
public class UserServiceImpl implements UserService{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserRepository userRepo;

    private final AccessCodeRepository accessCodeRepo;

    private final SeasonRepository seasonRepo;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepo, SeasonRepository seasonRepo, PasswordEncoder passwordEncoder, AccessCodeRepository accessCodeRepo) {
        this.userRepo = userRepo;
        this.seasonRepo = seasonRepo;
        this.passwordEncoder = passwordEncoder;
        this.accessCodeRepo = accessCodeRepo;
    }

    /** {@inheritDoc} */
    @Override
    public User getUser(Long id) {
        if (id == null || id < 1) {
            logger.error("Invalid user id #{}", id);
            throw new IllegalArgumentException("Id cannot be null or less than 1");
        }

        final Optional<User> user = userRepo.findById(id);
        if (user.isEmpty()) {
            logger.warn("getUser: User not found with id #{}", id);
            throw new NotFoundException("User not found");
        }

        logger.info("getUser: User found with id #{}", id);
        return user.get();
    }

    /** {@inheritDoc} */
    @Override
    public User getUserByUsername(String username) {
        username = username.toLowerCase().strip();

        // Check username is valid
        if (!UserService.validateUsername(username)) {
            logger.error("getUserByUsername: Invalid username #{}", username);
            throw new IllegalArgumentException("Invalid username " + username);
        }

        final Optional<User> user = userRepo.findByUsername(username);
        if (user.isEmpty()) {
            logger.warn("getUserByUsername: User not found with username #{}", username);
            throw new NotFoundException("User not found");
        }

        logger.info("getUserByUsername: Given username matches user #{}", user.get().getId());
        return user.get();
    }

    /** {@inheritDoc} */
    @Override
    public List<FirstNameDaysYear> getDaysSkied(Integer year) {
        // Not the best practice, but I highly doubt I'm going to be posting a season outside of these years
        if (year != null && ( year < 2000 || year > 2100)) {
            logger.error("getDaysSkied: Invalid year #{}", year);
            throw new IllegalArgumentException("Invalid year #" + year);
        }

        // If no year is passed, get the days skied data for all years
        if (year == null) {
            final List<FirstNameDaysYear> daysSkied = userRepo.getDaysSkied();

            if (daysSkied.isEmpty()) {
                logger.error("getDaysSkied: No days found for any year");
            }

            return daysSkied;
        }

        // Find season with given year as it's start year
        final Optional<Season> seasonFound = seasonRepo.findByStartYear(year);
        if (seasonFound.isEmpty()) {
            logger.error("getDaysSkied: No season found for year #{}", year);
            throw new NotFoundException("No season found for year " + year);
        }

        final Season season = seasonFound.get();

        final List<FirstNameDaysYear> daysSkied = userRepo.getDaysSkied(season.getStartDate(), season.getEndDate());

        if (daysSkied.isEmpty()) {
            logger.error("getDaysSkied: No days found for year #{}", year);
        }

        return daysSkied;
    }

    /** {@inheritDoc} */
    @Override
    public List<FirstNameDaysLocationYear> getDaysSkiedEachLocation(Integer year) {
        // Not the best practice, but I highly doubt I'm going to be posting a season outside of these years
        if (year != null && ( year < 2000 || year > 2100)) {
            logger.error("getDaysSkiedEachLocation: Invalid year #{}", year);
            throw new IllegalArgumentException("Invalid year #" + year);
        }

        // If no year is passed, get the days skied data for all years
        if (year == null) {
            final List<FirstNameDaysLocationYear> daysSkied = userRepo.getDaysSkiedEachLocation();

            if (daysSkied.isEmpty()) {
                logger.error("getDaysSkiedEachLocation: No days found for any year");
            }

            return daysSkied;
        }

        // Find season with given year as it's start year
        final Optional<Season> seasonFound = seasonRepo.findByStartYear(year);
        if (seasonFound.isEmpty()) {
            logger.error("getDaysSkiedEachLocation: No season found for year #{}", year);
            throw new NotFoundException("No season found for year " + year);
        }

        final Season season = seasonFound.get();

        final List<FirstNameDaysLocationYear> daysSkied = userRepo.getDaysSkiedEachLocation(season.getStartDate(), season.getEndDate());

        if (daysSkied.isEmpty()) {
            logger.error("getDaysSkiedEachLocation: No days found for year #{}", year);
        }

        return daysSkied;
    }

    /** {@inheritDoc} */
    public Optional<User> createUser(UserDto userDto){
        if (!UserService.validateUsername(userDto.username())){
            logger.warn("createUser: Invalid username #{}", userDto.username());
            return Optional.empty();
        }

        if (!UserService.validatePassword(userDto.password())){
            logger.warn("createUser: Invalid password");
            return Optional.empty();
        }

        if (userDto.firstName() == null || userDto.firstName().isEmpty()) {
            logger.warn("createUser: Invalid first name");
            return Optional.empty();
        }

        if (userDto.lastName() == null || userDto.lastName().isEmpty()) {
            logger.warn("createUser: Invalid last name");
            return Optional.empty();
        }

        if (!UserService.validateEmail(userDto.email())){
            logger.warn("createUser: Invalid email");
            return Optional.empty();
        }

        if (userRepo.existsByUsername(userDto.username())) {
            logger.warn("createUser: Username already exists");
            return Optional.empty();
        }

        if (userRepo.existsByEmail(userDto.email())) {
            logger.warn("createUser: Email already exists");
            return Optional.empty();
        }

        final User newUser = new User(
                userDto.username(),
                userDto.email(),
                this.passwordEncoder.encode(userDto.password()),
                userDto.firstName(),
                userDto.lastName(),
                userDto.profileImage()
        );

        try {
            final User savedUser = userRepo.save(newUser);
            logger.info("createUser: Saved user #{} to database", savedUser.getId());
            return Optional.of(savedUser);
        } catch(IllegalArgumentException | OptimisticLockException e) {
            logger.error("createUser: Error saving new user {}", e.getMessage());
            return Optional.empty();
        }
    }

    /** {@inheritDoc}*/
    @Override
    public boolean deleteUserByEmail(@Email String email) {
        if (email == null){
            return false;
        }

        if (!userRepo.existsByEmail(email)) {
            logger.warn("deleteUserByEmail: User not found with given email");
            return false;
        }

        userRepo.deleteUserByEmail(email);

        if (userRepo.existsByEmail(email)){
            logger.warn("deleteUserByEmail: Failed to delete user");
            return false;
        } else {
            logger.info("deleteUserByEmail: Deleted user");
            return true;
        }
    }

    /** {@inheritDoc}*/
    @Override
    public boolean validateAccessCode(@NotBlank String accessCode, @NotBlank String email) {
        final AccessCode code = accessCodeRepo.findByCode(accessCode.strip())
                .orElse(accessCodeRepo.findByEmail(email.strip()).orElse(null));

        if (code == null) {
            return false;
        }

        // Check if matches
        if (!code.getCode().equals(accessCode.strip()) || !code.getEmail().equals(email.strip())) {
            return false;
        }

        // Check if already used
        if (code.hasBeenUsed()) {
            return false;
        }

        // Check if expired
        if (LocalDateTime.now().isAfter(code.getExpiresAt())) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public AccessCode issueAccessCode(String email) {
        final String normalizedEmail = email.strip().toLowerCase();
        if (!UserService.validateEmail(normalizedEmail) || userRepo.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Invalid email or user already exists");
        }

        AccessCode code = accessCodeRepo.findByEmailForUpdate(normalizedEmail).orElse(null);
        if (code == null) {
            code = new AccessCode(normalizedEmail);
        } else {
            if (code.hasBeenUsed()) {
                throw new IllegalArgumentException("An account already used this invite");
            }
        }
        String candidate;
        do {
            candidate = AccessCode.generateCode();
        } while (candidate.equals(code.getCode()) || accessCodeRepo.existsByCode(candidate));
        code.renew(candidate);
        return accessCodeRepo.save(code);
    }

    @Override
    @Transactional
    public Optional<User> createUserWithAccessCode(UserDto userDto, String accessCode) {
        if (accessCode == null || accessCode.isBlank() || userDto.email() == null) {
            return Optional.empty();
        }
        final String normalizedEmail = userDto.email().strip().toLowerCase();
        final AccessCode code = accessCodeRepo.findByCodeForUpdate(accessCode.strip()).orElse(null);
        if (code == null || !code.getEmail().equals(normalizedEmail) || code.hasBeenUsed()
                || !LocalDateTime.now().isBefore(code.getExpiresAt())) {
            return Optional.empty();
        }

        final UserDto normalizedUser = new UserDto(userDto.username(), normalizedEmail, userDto.password(),
                userDto.firstName(), userDto.lastName(), userDto.profileImage());
        final Optional<User> user = createUser(normalizedUser);
        if (user.isEmpty()) {
            return Optional.empty();
        }
        code.setUsed();
        accessCodeRepo.save(code);
        return user;
    }
}
