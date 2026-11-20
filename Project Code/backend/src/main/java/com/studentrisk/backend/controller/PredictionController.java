package com.studentrisk.backend.controller;

import com.studentrisk.backend.dto.PredictionResponse;
import com.studentrisk.backend.dto.StudentRequest;
import com.studentrisk.backend.service.PredictionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping("/predict")
    public PredictionResponse predict(@Valid @RequestBody StudentRequest request) {
        return predictionService.predict(request);
    }

    @PostMapping("/students/{id}/predict")
    public PredictionResponse predictSavedStudent(@PathVariable Long id) {
        return predictionService.predictSavedStudent(id);
    }

    @GetMapping("/students/{id}/predict")
    public PredictionResponse getSavedStudentPrediction(@PathVariable Long id) {
        return predictionService.predictSavedStudent(id);
    }

    @GetMapping("/students/predictions")
    public Map<Long, PredictionResponse> getAllPredictions() {
        return predictionService.predictAllSavedStudents();
    }

    @PostMapping("/students/predict-all")
    public Map<Long, PredictionResponse> predictAllStudents() {
        return predictionService.predictAllSavedStudents();
    }
}
