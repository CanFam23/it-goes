package com.it_goes.api.service;

import com.it_goes.api.jpa.model.Season;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.projection.FirstNameDays;
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
    public List<FirstNameDays> getDaysSkied(int year) {
        // Not the best practice, but I highly doubt I'm going to be posting a season outside of these years
        if (year < 2000 || year > 2100) {
            logger.error("getDaysSkied: Invalid year #{}", year);
            throw new IllegalArgumentException("Invalid year #" + year);
        }

        final Optional<Season> seasonFound = seasonRepo.findByStartYear(year);
        if (seasonFound.isEmpty()) {
            logger.error("getDaysSkied: No season found for year #{}", year);
            throw new NotFoundException("No season found for year " + year);
        }

        final Season season = seasonFound.get();

        final List<FirstNameDays> daysSkied = userRepo.getDaysSkied(season.getStartDate(), season.getEndDate());

        if (daysSkied.isEmpty()) {
            logger.error("getDaysSkied: No days found for year #{}", year);
        }

        return daysSkied;
    }
}
