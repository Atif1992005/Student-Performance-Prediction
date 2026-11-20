package com.studentrisk.backend.service;

import com.studentrisk.backend.dto.PredictionResponse;
import com.studentrisk.backend.dto.StudentRequest;
import com.studentrisk.backend.exception.MlServiceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Service
public class FastApiPredictionClient {

    private final RestClient mlRestClient;

    public FastApiPredictionClient(RestClient mlRestClient) {
        this.mlRestClient = mlRestClient;
    }

    public PredictionResponse predict(StudentRequest features) {
        try {
            PredictionResponse response = mlRestClient.post()
                    .uri("/predict")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(features)
                    .retrieve()
                    .body(PredictionResponse.class);
            if (response == null || response.academicRisk() == null) {
                throw new MlServiceException(
                        "FastAPI returned an empty prediction.",
                        HttpStatus.BAD_GATEWAY
                );
            }
            return response;
        } catch (ResourceAccessException ex) {
            throw new MlServiceException(
                    "FastAPI ML service is unavailable at /predict.",
                    HttpStatus.SERVICE_UNAVAILABLE
            );
        } catch (RestClientResponseException ex) {
            String errorBody = ex.getResponseBodyAsString();
            String detail = (errorBody != null && !errorBody.isBlank()) ? ": " + errorBody : "";
            throw new MlServiceException(
                    "FastAPI /predict failed (" + ex.getStatusCode().value() + ")" + detail,
                    HttpStatus.BAD_GATEWAY
            );
        }
    }
}
