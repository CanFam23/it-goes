package com.it_goes.api.controller;

import com.it_goes.api.config.security.SecurityConfig;
import com.it_goes.api.config.security.jwt.AuthEntryPointJwt;
import com.it_goes.api.config.security.jwt.JwtUtils;
import com.it_goes.api.config.security.user.UserDetailsServiceImpl;
import com.it_goes.api.jpa.repo.LocationRepository;
import com.it_goes.api.jpa.model.Location;
import com.it_goes.api.service.SkiDayService;
import com.it_goes.api.service.LocationService;
import com.it_goes.api.util.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({SkiDayController.class, LocationController.class})
@Import({SecurityConfig.class, GlobalExceptionHandler.class, AuthEntryPointJwt.class, LocationService.class})
class SkiDayPathTests {
    @Autowired MockMvc mvc;
    @MockitoBean SkiDayService service;
    @MockitoBean LocationRepository locations;
    @MockitoBean UserDetailsServiceImpl userDetailsService;
    @MockitoBean JwtUtils jwtUtils;

    @Test
    void locationsArePublicButPersonalDaysAndWritesNeedLogin() throws Exception {
        when(locations.findAll()).thenReturn(List.of());
        mvc.perform(get("/locations")).andExpect(status().isOk()).andExpect(content().json("[]"));
        mvc.perform(get("/me/ski-days")).andExpect(status().isUnauthorized());
        mvc.perform(post("/me/ski-days").contentType("application/json")
                .content("{\"date\":\"2025-01-10\",\"locationId\":2}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/locations").contentType("application/json")
                .content("{\"name\":\"New\",\"state\":\"MT\",\"country\":\"US\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alice")
    void dayWritesUseTheAuthenticatedNameAndRejectCrossSiteOrigins() throws Exception {
        LocalDate date = LocalDate.of(2025, 1, 10);
        when(service.create("alice", date, 2L)).thenReturn(new SkiDayService.DayEntry(3L, date, 2L, "Beehive"));
        mvc.perform(post("/me/ski-days").header("Origin", "http://localhost:3000")
                .contentType("application/json").content("{\"date\":\"2025-01-10\",\"locationId\":2}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.id").value(3));
        verify(service).create("alice", date, 2L);

        mvc.perform(post("/me/ski-days").header("Origin", "https://other.example")
                .contentType("application/json").content("{\"date\":\"2025-01-10\",\"locationId\":2}"))
                .andExpect(status().isForbidden());
        mvc.perform(post("/me/ski-days")
                .contentType("application/json").content("{\"date\":\"2025-01-10\",\"locationId\":2}"))
                .andExpect(status().isForbidden());
        verifyNoMoreInteractions(service);
    }

    @Test
    @WithMockUser(username = "alice")
    void invalidLocationOptionsGiveAUsableBadRequest() throws Exception {
        mvc.perform(post("/locations").header("Origin", "http://localhost:3000")
                .contentType("application/json")
                .content("{\"name\":\"New\",\"state\":\"UNKNOWN\",\"country\":\"US\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "alice")
    void stateIsRequiredOnlyForUnitedStatesAndCountryIsAlwaysRequired() throws Exception {
        when(locations.save(any(Location.class))).thenAnswer(call -> call.getArgument(0));
        mvc.perform(post("/locations").header("Origin", "http://localhost:3000")
                .contentType("application/json")
                .content("{\"name\":\"Chamonix\",\"country\":\"FR\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.country").value("FR"))
                .andExpect(jsonPath("$.state").value(org.hamcrest.Matchers.nullValue()));
        mvc.perform(post("/locations").header("Origin", "http://localhost:3000")
                .contentType("application/json")
                .content("{\"name\":\"Big Sky\",\"country\":\"US\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/locations").header("Origin", "http://localhost:3000")
                .contentType("application/json")
                .content("{\"name\":\"Mystery\"}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/locations")
                .contentType("application/json")
                .content("{\"name\":\"No origin\",\"country\":\"FR\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void countryOptionsIncludeEuropeanSkiDestinations() throws Exception {
        mvc.perform(get("/locations/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.countries[?(@.value == 'FR')]").isNotEmpty())
                .andExpect(jsonPath("$.countries[?(@.value == 'IT')]").isNotEmpty())
                .andExpect(jsonPath("$.countries[?(@.value == 'CH')]").isNotEmpty())
                .andExpect(jsonPath("$.countries[?(@.value == 'NO')]").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "alice")
    void skiDayRequiresDateAndLocation() throws Exception {
        mvc.perform(post("/me/ski-days").header("Origin", "http://localhost:3000")
                .contentType("application/json").content("{\"locationId\":2}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/me/ski-days").header("Origin", "http://localhost:3000")
                .contentType("application/json").content("{\"date\":\"2025-01-10\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
