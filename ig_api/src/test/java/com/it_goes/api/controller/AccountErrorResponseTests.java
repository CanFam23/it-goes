package com.it_goes.api.controller;

import com.it_goes.api.service.UserService;
import com.it_goes.api.util.exception.AccountRequestException;
import com.it_goes.api.util.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountErrorResponseTests {
    private UserService userService;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        AuthController authController = new AuthController();
        ReflectionTestUtils.setField(authController, "userService", userService);
        mvc = MockMvcBuilders.standaloneSetup(authController, new AccessCodeController(userService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void duplicateUsernameReturnsConflictWithReadableMessage() throws Exception {
        when(userService.createUserWithAccessCode(any(), anyString()))
                .thenThrow(new AccountRequestException(HttpStatus.CONFLICT, "Username is already taken."));

        mvc.perform(post("/auth/signup").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"accessCode\":\"ABC12345\",\"username\":\"friend\",\"email\":\"friend@test.com\",\"password\":\"Valid123!\",\"confirmPassword\":\"Valid123!\",\"firstName\":\"First\",\"lastName\":\"Last\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Username is already taken."));
    }

    @Test
    void inviteForRegisteredEmailReturnsConflictWithReadableMessage() throws Exception {
        when(userService.issueAccessCode("friend@test.com"))
                .thenThrow(new AccountRequestException(HttpStatus.CONFLICT, "An account already exists for this email."));

        mvc.perform(post("/access-codes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"friend@test.com\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("An account already exists for this email."));
    }

    @Test
    void invalidInviteEmailReturnsReadableValidationMessage() throws Exception {
        mvc.perform(post("/access-codes").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Enter a valid email address."));
    }
}
