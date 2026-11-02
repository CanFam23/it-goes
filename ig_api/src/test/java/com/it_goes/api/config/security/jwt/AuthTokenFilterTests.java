package com.it_goes.api.config.security.jwt;

import com.it_goes.api.config.security.user.UserDetailsServiceImpl;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuthTokenFilterTests {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validLoginCookieAuthenticatesRequest() throws Exception {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserDetailsServiceImpl userDetailsService = mock(UserDetailsServiceImpl.class);
        AuthTokenFilter filter = new AuthTokenFilter();
        ReflectionTestUtils.setField(filter, "jwtUtils", jwtUtils);
        ReflectionTestUtils.setField(filter, "userDetailsService", userDetailsService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("token", "signed-token"));
        when(jwtUtils.validateJwtToken("signed-token")).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken("signed-token")).thenReturn("nclouse");
        when(userDetailsService.loadUserByUsername("nclouse"))
                .thenReturn(new com.it_goes.api.config.security.user.UserDetailsImpl(
                        new com.it_goes.api.jpa.model.User("nclouse", "nick@test.com", "hash", "Nick", "Clouse", null)));

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("nclouse");
    }
}
