package com.it_goes.api.service;

import com.it_goes.api.config.security.user.UserDetailsImpl;
import com.it_goes.api.config.security.user.UserDetailsServiceImpl;
import com.it_goes.api.dto.UserDto;
import com.it_goes.api.jpa.model.User;
import com.it_goes.api.jpa.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsernameFoundUser() {
        final String username = "username";

        User user = new User(username, "email@test.com", "hashedPw", "f", "l", null);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetailsImpl details =
                (UserDetailsImpl) userDetailsService.loadUserByUsername(username);

        assertEquals(user, details.getUser());
        assertEquals(username, details.getUsername());
        assertEquals("hashedPw", details.getPassword());
        assertEquals(0, details.getAuthorities().size());
        assertTrue(details.isAccountNonExpired());
        assertTrue(details.isAccountNonLocked());
        assertTrue(details.isCredentialsNonExpired());
        assertTrue(details.isEnabled());
    }

    @Test
    void loadUserByUsernameFoundUserWithSpaces() {
        final String stored = "usernameTwo";

        User user = new User(stored, "email", "hashedPw", "f", "l", null);

        when(userRepository.findByUsername(stored)).thenReturn(Optional.of(user));

        UserDetailsImpl details =
                (UserDetailsImpl) userDetailsService.loadUserByUsername(stored);

        assertEquals(user, details.getUser());
    }

    @Test
    void loadUserByUsernameNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrowsExactly(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("unknown")
        );
    }

    @Test
    void loadUserByUsernameEmptyString() {
        assertThrowsExactly(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("")
        );
    }

    @Test
    void loadUserByUsernameNull() {
        assertThrowsExactly(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(null)
        );
    }

    @Test
    void loadUserByUsernameDeletedUserNotFound() {
        final String username = "usernameThree";

        when(userRepository.findByUsername(username))
                .thenReturn(Optional.empty());

        assertThrowsExactly(
                UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername(username)
        );
    }

    @Test
    void loadUserByUsernameTwoUsersReturnCorrectOne() {
        final String u1 = "usernameOne";
        final String u2 = "usernameTwo";

        User user1 = new User(u1, "e1", "p1", "f1", "l1", null);
        User user2 = new User(u2, "e2", "p2", "f2", "l2", null);

        when(userRepository.findByUsername(u1)).thenReturn(Optional.of(user1));
        when(userRepository.findByUsername(u2)).thenReturn(Optional.of(user2));

        UserDetailsImpl d1 = (UserDetailsImpl) userDetailsService.loadUserByUsername(u1);
        assertEquals(user1, d1.getUser());

        UserDetailsImpl d2 = (UserDetailsImpl) userDetailsService.loadUserByUsername(u2);
        assertEquals(user2, d2.getUser());
    }
}
