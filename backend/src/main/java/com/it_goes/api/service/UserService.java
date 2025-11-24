package com.it_goes.api.service;

import com.it_goes.api.dto.UserDto;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.projection.FirstNameDaysLocationYear;
import com.it_goes.api.jpa.projection.FirstNameDaysYear;
import com.it_goes.api.util.exception.NotFoundException;
import jakarta.validation.constraints.Email;

import java.util.List;
import java.util.Optional;

public interface UserService {
    int MIN_USERNAME_LENGTH = 3;
    int MAX_USERNAME_LENGTH = 20;

    static boolean validateUsername(String username){
        username = username.strip().toLowerCase();

        if(username.isBlank()){
            return false;
        }

        if(username.contains(" ")){
            return false;
        }

        // Check for non-alphanumeric characters
        for (int i = 0; i < username.length(); i++) {
            char c = username.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                return false; // Found a non-alphanumeric character
            }
        }

        if(username.length() < MIN_USERNAME_LENGTH || username.length() > MAX_USERNAME_LENGTH){
            return false;
        }

        return true;
    }


    /**
     * Gets user by user id.
     * @param id ID to search for
     * @return User object of user found
     * @throws NotFoundException If a user with the given ID isn't found
     * @throws IllegalArgumentException If given id is null or less than 1
     */
    User getUser(Long id);

    /**
     * Gets user by username.
     * @param username username to search for
     * @return User object of user found
     * @throws NotFoundException If a user with the given username isn't found
     * @throws IllegalArgumentException If an invalid username is given
     *
     * @see #validateUsername(String)
     */
    User getUserByUsername(String username);

    /**
     * Gets the number of days each user has skied in the season beginning in the given year.
     * @param year Year the season starts in (ex. 2024-2025 season, you would pass 2024)
     * @return List of {@link FirstNameDaysYear} object which contain the first name of the user, the number of days they've skied, and the year.
     */
    List<FirstNameDaysYear> getDaysSkied(Integer year);

    /**
     * Gets the number of days each user has skied at each location in the season beginning in the given year.
     * @param year Year the season starts in (ex. 2024-2025 season, you would pass 2024)
     * @return List of {@link FirstNameDaysLocationYear} object which contain the first name of the user, the number of days they've skied,
     * the name of each location they've skied at, and the year.
     */
    List<FirstNameDaysLocationYear> getDaysSkiedEachLocation(Integer year);

    /**
     * Create and save a new user with the information in the given {@link UserDto} object.
     * @param userDto Contains the information for the new user/
     * @return User object if created, empty optional if not.
     */
    Optional<User> createUser(UserDto userDto);

    /**
     * Deletes a user with the given email if found.
     * @param email Email of user to delete.
     * @return {@code true} if user was deleted, {@code false} otherwise.
     */
    boolean deleteUserByEmail(@Email String email);
}
