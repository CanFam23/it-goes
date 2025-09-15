package com.it_goes.api.service;

import com.it_goes.api.jpa.model.Season;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.projection.FirstNameDaysLocationYear;
import com.it_goes.api.jpa.projection.FirstNameDaysYear;
import com.it_goes.api.jpa.repo.SeasonRepository;
import com.it_goes.api.jpa.repo.UserRepository;
import com.it_goes.api.util.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserRepository userRepo;

    private final SeasonRepository seasonRepo;

    public UserServiceImpl(UserRepository userRepo, SeasonRepository seasonRepo) {
        this.userRepo = userRepo;
        this.seasonRepo = seasonRepo;
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
            logger.warn("getUser: User not found with username #{}", username);
            throw new NotFoundException("User not found");
        }

        logger.info("getUser: User found with username #{}", username);
        return user.get();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
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
}
