package com.it_goes.api.service;

import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.projection.FirstNameDays;
import com.it_goes.api.util.exception.NotFoundException;

import java.util.List;

public interface UserService {
    /**
     * Gets user by user id.
     * @param id ID to search for
     * @return User object of user found
     * @throws NotFoundException If a user with the given ID isn't found
     */
    User getUser(Long id);

    /**
     * Gets user by username.
     * @param username username to search for
     * @return User object of user found
     * @throws NotFoundException If a user with the given username isn't found
     */
    User getUserByUsername(String username);

    /**
     * Gets the number of days each user has skied in the season beginning in the given year.
     * @param year Year the season starts in (ex. 2024-2025 season, you would pass 2024)
     * @return List of {@link FirstNameDays} object which contain the first name of the user and the number of days they've skied.
     */
    List<FirstNameDays> getDaysSkied(int year);
}
