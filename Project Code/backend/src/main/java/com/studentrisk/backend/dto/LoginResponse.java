package com.studentrisk.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginResponse(
        @JsonProperty("username")
        String username,

        @JsonProperty("fullName")
        String fullName,

        @JsonProperty("role")
        String role,

        @JsonProperty("studentId")
        Long studentId,

        @JsonProperty("token")
        String token,

        @JsonProperty("message")
        String message
) {
}
