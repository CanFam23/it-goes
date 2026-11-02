package com.it_goes.api.controller;

import com.it_goes.api.jpa.model.AccessCode;
import com.it_goes.api.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/access-codes")
public class AccessCodeController {
    private final UserService userService;

    public AccessCodeController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<AccessCodeResponse> create(@Valid @RequestBody AccessCodeRequest request) {
        AccessCode code = userService.issueAccessCode(request.email());
        return ResponseEntity.ok(new AccessCodeResponse(code.getCode(), code.getEmail(), code.getExpiresAt()));
    }

    public record AccessCodeRequest(@NotBlank @Email String email) {}

    public record AccessCodeResponse(String code, String email, LocalDateTime expiresAt) {}
}
