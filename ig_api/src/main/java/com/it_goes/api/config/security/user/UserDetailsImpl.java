package com.it_goes.api.config.security.user;

import com.it_goes.api.jpa.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public record UserDetailsImpl(User user) implements UserDetails {
    /**
     * Gets the users authorities. We don't use roles in our application, so
     * it just returns an empty list.
     *
     * @return Empty list, because we don't use roles
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    /**
     * Gets the users hashed password
     *
     * @return User's hashed password
     */
    @Override
    public String getPassword() {
        return user.getHashedPasswordString();
    }

    /**
     * Gets the users username
     *
     * @return User's Username
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Gets the user object
     *
     * @return User object
     */
    public User getUser() {
        return user;
    }

    /**
     * Returns if account is not expired
     *
     * @return true
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Returns if account is not locked
     *
     * @return true
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Returns if credentials are not expired
     *
     * @return true
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Returns if user is enabled
     *
     * @return true
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

    /**
     * Converts CustomUserDetails obj to string
     *
     * @return String version of CustomUserDetails
     */
    @Override
    public String toString() {
        return "User id#" + user.getId();
    }
}