package com.it_goes.api.service;

import com.it_goes.api.config.security.user.UserDetailsImpl;
import com.it_goes.api.config.security.user.UserDetailsServiceImpl;
import com.it_goes.api.dto.UserDto;
import com.it_goes.api.jpa.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the CustomUserDetailsService class
 */
@SpringBootTest
public class UserDetailsServiceTests {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UserService userService;

    @Test
    public void loadUserByUsernameFoundUser() {
        final String username = "username";
        final UserDto userDto = new UserDto(username, "email@email.com","password", "first", "last", null);
        final User savedUser = userService.createUser(userDto).orElseThrow(() -> new UsernameNotFoundException(username));
        assertNotNull(savedUser, "User should've been saved in database when testing loadUserByUsernameFoundUser");

        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        assertEquals(savedUser, userDetails.getUser(), "User object stored in CustomUserDetails should match the object returned from saveUser");
        assertEquals(savedUser.getUsername(), userDetails.getUsername(), "User should be found since user is in database!");
        assertEquals(savedUser.getHashedPasswordString(), userDetails.getPassword(), "Users hashed password should be stored in customUserDetails!");
        assertEquals(userDetails.getAuthorities().size(), 0, "User should have no authorities in CustomUserDetails object!");
        assertEquals(savedUser, userDetails.getUser(), "Returned user object should match the saved user object");
        assertTrue(userDetails.isAccountNonExpired(), "User's account should not be expired!");
        assertTrue(userDetails.isAccountNonLocked(), "User's account should not be locked!");
        assertTrue(userDetails.isCredentialsNonExpired(), "User's credentials should not be expired!");
        assertTrue(userDetails.isEnabled(), "User's enabled should be enabled!");
    }

    @Test
    public void loadUserByUsernameFoundUserLeadingTrailingSpaces() {
        final String username = "   usernameTwo   ";
        final UserDto userDto = new UserDto(username, "emailTwo@email.com","password", "first", "last", null);
        final User savedUser = userService.createUser(userDto).orElseThrow(() -> new UsernameNotFoundException(username));
        assertNotEquals(savedUser, null, "User should've been saved in database when testing loadUserByUsernameFoundUserLeadingTrailingSpaces");

        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        assertEquals(savedUser, userDetails.getUser(), "User object stored in CustomUserDetails should match the object returned from saveUser");
        assertEquals(savedUser.getUsername(), userDetails.getUsername(), "User should be found since user is in database!");
        assertEquals(savedUser.getHashedPasswordString(), userDetails.getPassword(), "Users hashed password should be stored in customUserDetails!");
        assertEquals(userDetails.getAuthorities().size(), 0, "User should have no authorities in CustomUserDetails object!");
        assertEquals(savedUser, userDetails.getUser(), "Returned user object should match the saved user object");
        assertTrue(userDetails.isAccountNonExpired(), "User's account should not be expired!");
        assertTrue(userDetails.isAccountNonLocked(), "User's account should not be locked!");
        assertTrue(userDetails.isCredentialsNonExpired(), "User's credentials should not be expired!");
        assertTrue(userDetails.isEnabled(), "User's enabled should be enabled!");
    }

    @Test
    public void loadUserByUsernameNotFoundUser() {
        final String username = "unknownUser";

        assertThrowsExactly(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(username), "Exception should be thrown since no user is in database!");
    }

    @Test
    public void loadUserByUsernameEmptyUsername() {
        final String username = "";

        assertThrowsExactly(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(username), "Exception should be thrown since username is empty when loading by username!");
    }

    @Test
    public void loadUserByUsernameNullUsername() {
        assertThrowsExactly(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(null), "Exception should be thrown since username is null when loading by username!");
    }

    @Test
    public void loadUserByUsernameNotFoundUserSpaceInUserName() {
        final String username = "user name";

        assertThrowsExactly(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(username), "Exception should be thrown since username is not in database!");
    }

    @Test
    @Transactional
    public void loadUserByUsernameDeletedUserNotFound() {
        final String username = "usernameThree";
        final UserDto userDto = new UserDto(username, "emailThree@email.com","password", "first", "last", null);

        final User savedUser = userService.createUser(userDto).orElseThrow(() -> new UsernameNotFoundException(username));
        assertNotEquals(savedUser, null, "User should've been saved in database when testing loadUserByUsernameDeletedUserNotFound");

        final boolean deletedUser = userService.deleteUserByEmail(savedUser.getEmail());
        assertTrue(deletedUser, "User should be deleted since user is in database and a valid email was passed!");

        assertThrowsExactly(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername(username), "Exception should be thrown since username no longer exists in database!");
    }

    @Test
    @Transactional
    public void loadUserByUsernameDeletedUserThenAddedAgainFound() {
        final String username = "usernameFour";

        final UserDto userDto = new UserDto(username, "emailFour@email.com","password", "first", "last", null);

        //save user
        User savedUser = userService.createUser(userDto).orElseThrow(() -> new UsernameNotFoundException(username));
        assertNotEquals(savedUser, null, "User should've been saved in database when testing loadUserByUsernameDeletedUserThenAddedAgainFound");

        //delete user
        final boolean deletedUser = userService.deleteUserByEmail(savedUser.getEmail());
        assertTrue(deletedUser, "User should be deleted since user is in database and a valid email was passed!");

        //resave user
        savedUser = userService.createUser(userDto).orElseThrow(() -> new UsernameNotFoundException(username));
        assertNotEquals(savedUser, null, "User should've been resaved in database when testing loadUserByUsernameDeletedUserThenAddedAgainFound");

        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        assertEquals(savedUser, userDetails.getUser(), "User object stored in CustomUserDetails should match the object returned from saveUser");
        assertEquals(savedUser.getUsername(), userDetails.getUsername(), "User should be found since user is in database!");
        assertEquals(savedUser.getHashedPasswordString(), userDetails.getPassword(), "Users hashed password should be stored in customUserDetails!");
        assertEquals(userDetails.getAuthorities().size(), 0, "User should have no authorities in CustomUserDetails object!");
        assertEquals(savedUser, userDetails.getUser(), "Returned user object should match the saved user object");
        assertTrue(userDetails.isAccountNonExpired(), "User's account should not be expired!");
        assertTrue(userDetails.isAccountNonLocked(), "User's account should not be locked!");
        assertTrue(userDetails.isCredentialsNonExpired(), "User's credentials should not be expired!");
        assertTrue(userDetails.isEnabled(), "User's enabled should be enabled!");
    }

    @Test
    public void loadUserByUsernameWhenTwoSavedUsersReturnsCorrectUser() {
        final String username = "usernameFive";
        final UserDto userDto = new UserDto(username, "emailFive@email.com", "password", "first", "last", null);
        User savedUser = userService.createUser(userDto).orElseThrow(() -> new UsernameNotFoundException(username));
        assertNotEquals(savedUser, null, "User should've been saved in database when testing loadUserByUsernameWhenTwoSavedUsersReturnsCorrectUser");

        final String usernameTwo = "usernameSix";
        final UserDto userDtoTwo = new UserDto(usernameTwo, "emailSix@email.com","password", "first", "last", null);
        User savedUserTwo = userService.createUser(userDtoTwo).orElseThrow(() -> new UsernameNotFoundException(usernameTwo));
        assertNotEquals(savedUserTwo, null, "User should've been saved in database when testing loadUserByUsernameWhenTwoSavedUsersReturnsCorrectUser");

        //Search for the first saved user, should return its info
        final UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        assertEquals(savedUser, userDetails.getUser(), "User object stored in CustomUserDetails should match the object returned from saveUser");
        assertEquals(savedUser.getUsername(), userDetails.getUsername(), "User should be found since user is in database!");
        assertEquals(savedUser.getHashedPasswordString(), userDetails.getPassword(), "Users hashed password should be stored in customUserDetails!");
        assertEquals(userDetails.getAuthorities().size(), 0, "User should have no authorities in CustomUserDetails object!");
        assertEquals(savedUser, userDetails.getUser(), "Returned user object should match the saved user object");
        assertTrue(userDetails.isAccountNonExpired(), "User's account should not be expired!");
        assertTrue(userDetails.isAccountNonLocked(), "User's account should not be locked!");
        assertTrue(userDetails.isCredentialsNonExpired(), "User's credentials should not be expired!");
        assertTrue(userDetails.isEnabled(), "User's enabled should be enabled!");

        //Search for the second saved user, should return its info
        final UserDetailsImpl userDetailsTwo = (UserDetailsImpl) userDetailsService.loadUserByUsername(usernameTwo);

        assertEquals(savedUserTwo, userDetailsTwo.getUser(), "User object stored in CustomUserDetails should match the object returned from saveUser");
        assertEquals(savedUserTwo.getUsername(), userDetailsTwo.getUsername(), "User should be found since user is in database!");
        assertEquals(savedUserTwo.getHashedPasswordString(), userDetailsTwo.getPassword(), "Users hashed password should be stored in customUserDetails!");
        assertEquals(userDetailsTwo.getAuthorities().size(), 0, "User should have no authorities in CustomUserDetails object!");
        assertEquals(savedUserTwo, userDetailsTwo.getUser(), "Returned user object should match the saved user object");
        assertTrue(userDetailsTwo.isAccountNonExpired(), "User's account should not be expired!");
        assertTrue(userDetailsTwo.isAccountNonLocked(), "User's account should not be locked!");
        assertTrue(userDetailsTwo.isCredentialsNonExpired(), "User's credentials should not be expired!");
        assertTrue(userDetailsTwo.isEnabled(), "User's enabled should be enabled!");
    }
}
