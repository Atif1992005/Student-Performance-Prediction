package com.studentrisk.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @JsonProperty("username")
        @NotBlank(message = "Username/ID cannot be empty")
        String username,

        @JsonProperty("password")
        @NotBlank(message = "Password cannot be empty")
        String password
) {
}
