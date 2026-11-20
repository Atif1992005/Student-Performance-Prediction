package com.studentrisk.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ModelScoreResponse(
        String model,
        @JsonProperty("Academic_Risk") String academicRisk,
        Map<String, Double> probabilities
) {
}
