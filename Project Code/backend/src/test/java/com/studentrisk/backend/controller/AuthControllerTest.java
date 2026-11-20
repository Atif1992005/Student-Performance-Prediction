package com.studentrisk.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentrisk.backend.dto.LoginRequest;
import com.studentrisk.backend.dto.LoginResponse;
import com.studentrisk.backend.exception.InvalidCredentialsException;
import com.studentrisk.backend.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void login_ValidFaculty_Returns200() throws Exception {
        LoginRequest req = new LoginRequest("F0001", "password123");
        LoginResponse res = new LoginResponse("F0001", "Dr. Sharma", "FACULTY", null, "token_123", "OK");

        when(authService.login(any(LoginRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("F0001"))
                .andExpect(jsonPath("$.role").value("FACULTY"))
                .andExpect(jsonPath("$.token").value("token_123"));
    }

    @Test
    void login_ValidStudent_Returns200() throws Exception {
        LoginRequest req = new LoginRequest("1", "password123");
        LoginResponse res = new LoginResponse("1", "Student (Roll No. 1)", "STUDENT", 1L, "token_456", "OK");

        when(authService.login(any(LoginRequest.class))).thenReturn(res);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("1"))
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.studentId").value(1));
    }

    @Test
    void login_InvalidPassword_Returns401() throws Exception {
        LoginRequest req = new LoginRequest("F0001", "wrongpass");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new InvalidCredentialsException("Invalid password for F0001."));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid password for F0001."));
    }

    @Test
    void login_BlankFields_Returns400() throws Exception {
        LoginRequest req = new LoginRequest("", "");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"));
    }
}
