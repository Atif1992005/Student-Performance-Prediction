package com.studentrisk.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentrisk.backend.dto.ModelScoreResponse;
import com.studentrisk.backend.dto.PredictionResponse;
import com.studentrisk.backend.dto.StudentRequest;
import com.studentrisk.backend.exception.MlServiceException;
import com.studentrisk.backend.exception.ResourceNotFoundException;
import com.studentrisk.backend.service.PredictionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PredictionController.class)
class PredictionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PredictionService predictionService;

    private StudentRequest sampleRequest() {
        return new StudentRequest(
                7.35, 2, 19.6, 66.3,
                54.7, 62.1, 61.9, 41.0, 20.0
        );
    }

    private PredictionResponse samplePrediction() {
        return new PredictionResponse(
                "Medium",
                Map.of("Low", 0.012, "Medium", 0.754, "High", 0.234),
                "Logistic Regression",
                new ModelScoreResponse(
                        "Random Forest",
                        "Medium",
                        Map.of("Low", 0.04, "Medium", 0.875, "High", 0.085)
                )
        );
    }

    @Test
    void predict_ValidFeatures_ReturnsPrediction() throws Exception {
        when(predictionService.predict(any(StudentRequest.class))).thenReturn(samplePrediction());

        mockMvc.perform(post("/api/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Academic_Risk").value("Medium"))
                .andExpect(jsonPath("$.probabilities.Medium").value(0.754))
                .andExpect(jsonPath("$.model").value("Logistic Regression"))
                .andExpect(jsonPath("$.comparison.Academic_Risk").value("Medium"));
    }

    @Test
    void predict_WithoutCgpa_ReturnsPrediction() throws Exception {
        StudentRequest reqWithoutCgpa = new StudentRequest(
                null, 2, 19.6, 66.3,
                54.7, 62.1, 61.9, 41.0, 20.0
        );
        when(predictionService.predict(any(StudentRequest.class))).thenReturn(samplePrediction());

        mockMvc.perform(post("/api/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqWithoutCgpa)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Academic_Risk").value("Medium"))
                .andExpect(jsonPath("$.probabilities.Medium").value(0.754))
                .andExpect(jsonPath("$.model").value("Logistic Regression"));
    }

    @Test
    void predictSavedStudent_Post_ReturnsPrediction() throws Exception {
        when(predictionService.predictSavedStudent(eq(1L))).thenReturn(samplePrediction());

        mockMvc.perform(post("/api/students/1/predict"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Academic_Risk").value("Medium"))
                .andExpect(jsonPath("$.probabilities.Medium").value(0.754));
    }

    @Test
    void predictSavedStudent_Get_ReturnsPrediction() throws Exception {
        when(predictionService.predictSavedStudent(eq(1L))).thenReturn(samplePrediction());

        mockMvc.perform(get("/api/students/1/predict"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Academic_Risk").value("Medium"))
                .andExpect(jsonPath("$.probabilities.Medium").value(0.754));
    }

    @Test
    void getAllPredictions_Get_ReturnsMap() throws Exception {
        when(predictionService.predictAllSavedStudents()).thenReturn(Map.of(1L, samplePrediction()));

        mockMvc.perform(get("/api/students/predictions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.1.Academic_Risk").value("Medium"));
    }

    @Test
    void predictAllStudents_Post_ReturnsMap() throws Exception {
        when(predictionService.predictAllSavedStudents()).thenReturn(Map.of(1L, samplePrediction()));

        mockMvc.perform(post("/api/students/predict-all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.1.Academic_Risk").value("Medium"));
    }

    @Test
    void predictSavedStudent_NotFound_Returns404() throws Exception {
        when(predictionService.predictSavedStudent(eq(999L)))
                .thenThrow(new ResourceNotFoundException("Student not found: 999"));

        mockMvc.perform(post("/api/students/999/predict"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void predict_MlServiceUnavailable_Returns503() throws Exception {
        when(predictionService.predict(any(StudentRequest.class)))
                .thenThrow(new MlServiceException("FastAPI ML service is unavailable at /predict.", HttpStatus.SERVICE_UNAVAILABLE));

        mockMvc.perform(post("/api/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleRequest())))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error").value("ml_service_error"));
    }

    @Test
    void predict_InvalidPayload_Returns400ValidationFailed() throws Exception {
        // CGPA > 10 is invalid
        String invalidJson = """
                {
                  "CGPA": 15.0,
                  "Backlogs": 0,
                  "Previous_Semester_Internal_Marks": 20.0,
                  "Previous_Semester_Attendance": 75.0,
                  "Previous_Semester_Assignment_Score": 70.0,
                  "Previous_Semester_Test_Score": 70.0,
                  "Current_Semester_Attendance": 75.0,
                  "Current_Semester_Test_Score": 70.0,
                  "Assignment_Completion_Percent": 80.0
                }
                """;

        mockMvc.perform(post("/api/predict")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"))
                .andExpect(jsonPath("$.fields.cgpa").exists());
    }
}
