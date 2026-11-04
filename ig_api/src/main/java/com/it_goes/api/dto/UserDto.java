package com.it_goes.api.dto;

import com.it_goes.api.jpa.model.Image;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserDto(@NotBlank String username,
                      @Email  String email,
                      @NotBlank String password,
                      @NotBlank String firstName,
                      @NotBlank String lastName,
                      Image profileImage) {
}
