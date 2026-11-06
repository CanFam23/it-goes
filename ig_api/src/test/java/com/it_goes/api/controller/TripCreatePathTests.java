package com.it_goes.api.controller;

import com.it_goes.api.config.security.SecurityConfig;
import com.it_goes.api.config.security.jwt.AuthEntryPointJwt;
import com.it_goes.api.config.security.jwt.JwtUtils;
import com.it_goes.api.config.security.user.UserDetailsServiceImpl;
import com.it_goes.api.service.TripService;
import com.it_goes.api.dto.TripDto;
import com.it_goes.api.util.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@WebMvcTest(TripController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AuthEntryPointJwt.class})
class TripCreatePathTests {
    @Autowired MockMvc mvc;
    @MockitoBean TripService trips;
    @MockitoBean UserDetailsServiceImpl userDetailsService;
    @MockitoBean JwtUtils jwtUtils;

    private static final String BODY = """
            {"title":"A trip","dateOfTrip":"2025-01-10","locationId":2,"participantIds":[],
             "body":{"root":{"type":"root","children":[{"type":"paragraph","children":[{"type":"text","text":"Hello","format":0}]}]}}}
            """;

    @Test
    void anonymousTripCreationIsRejected() throws Exception {
        mvc.perform(post("/trips").header("Origin", "http://localhost:3000")
                .contentType("application/json").content(BODY)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alice")
    void tripCreationRequiresAllowedOrigin() throws Exception {
        mvc.perform(post("/trips").header("Origin", "https://other.example")
                .contentType("application/json").content(BODY)).andExpect(status().isForbidden());
        mvc.perform(post("/trips").contentType("application/json").content(BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alice")
    void tripCreationUsesSignedInAuthorAndReturnsLocation() throws Exception {
        when(trips.createTrip(eq("alice"), any())).thenReturn(new TripDto(7L, "A trip", "Hello", "Beehive", "MT",
                java.time.LocalDate.of(2025, 1, 10), null, null, "alice", new String[0], null, null,
                null, "Alice Smith", new String[0]));
        mvc.perform(post("/api/trips").contextPath("/api").header("Origin", "http://localhost:3000")
                .contentType("application/json").content(BODY))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/trips/7"))
                .andExpect(jsonPath("$.id").value(7));
        verify(trips).createTrip(eq("alice"), any());
    }
}
