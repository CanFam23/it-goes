package com.it_goes.api.controller;

import com.it_goes.api.config.security.SecurityConfig;
import com.it_goes.api.config.security.jwt.AuthEntryPointJwt;
import com.it_goes.api.config.security.jwt.JwtUtils;
import com.it_goes.api.config.security.user.UserDetailsServiceImpl;
import com.it_goes.api.service.TripService;
import com.it_goes.api.dto.TripDto;
import com.it_goes.api.service.UserService;
import com.it_goes.api.util.exception.GlobalExceptionHandler;
import com.it_goes.api.util.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.time.LocalDate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({TripController.class, UserController.class, AccessCodeController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AuthEntryPointJwt.class})
class PublicReadPathTests {
    @Autowired MockMvc mvc;
    @MockitoBean TripService tripService;
    @MockitoBean UserService userService;
    @MockitoBean UserDetailsServiceImpl userDetailsService;
    @MockitoBean JwtUtils jwtUtils;

    @Test
    void anonymousCanReadPublicEndpoints() throws Exception {
        when(tripService.getTripSummaries(3, 0)).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 3), 0));
        when(userService.getDaysSkied(null)).thenReturn(List.of());
        when(userService.getDaysSkiedEachLocation(null)).thenReturn(List.of());
        when(tripService.getAllTripFeatureRoutes()).thenReturn(new ObjectMapper().readTree("{\"type\":\"FeatureCollection\",\"features\":[]}"));
        mvc.perform(get("/getTrips")).andExpect(status().isOk())
                .andExpect(jsonPath("$.trips").isEmpty())
                .andExpect(jsonPath("$.numPages").value(0));
        mvc.perform(get("/getDaysSkied")).andExpect(status().isOk());
        mvc.perform(get("/getDaysSkiedLocation")).andExpect(status().isOk());
        mvc.perform(get("/getAllTripFeatureRoutes")).andExpect(status().isOk());
    }

    @Test
    void anonymousTripDetailIncludesCoverAndNullableRoute() throws Exception {
        when(tripService.getTrip(7L)).thenReturn(new TripDto(
                7L, "A trip", "A good day", "Big Sky", "Montana",
                LocalDate.of(2025, 1, 1), null, null, "author",
                new String[]{"friend"}, null, new double[]{-111.4, 45.3},
                "https://example.com/cover.jpg"));

        mvc.perform(get("/getTrip?id=7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.imageURL").value("https://example.com/cover.jpg"))
                .andExpect(jsonPath("$.route").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void anonymousCannotWriteInvites() throws Exception {
        mvc.perform(post("/access-codes").contentType("application/json")
                .content("{\"email\":\"friend@example.com\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication required."));
        mvc.perform(post("/getTrips"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void badPageAndMissingTripReturnJsonMessages() throws Exception {
        mvc.perform(get("/getTrips?pageNum=oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
        when(tripService.getTrip(999L)).thenThrow(new NotFoundException("Trip with ID #999 not found"));
        mvc.perform(get("/getTrip?id=999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Trip with ID #999 not found"));
        mvc.perform(get("/getTrip?id=invalid"))
                .andExpect(status().isBadRequest());
    }
}
