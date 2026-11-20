package com.studentrisk.backend.service;

import com.studentrisk.backend.dto.PredictionResponse;
import com.studentrisk.backend.dto.StudentRequest;
import com.studentrisk.backend.entity.Student;
import com.studentrisk.backend.exception.ResourceNotFoundException;
import com.studentrisk.backend.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PredictionService {

    private final FastApiPredictionClient fastApiPredictionClient;
    private final StudentRepository studentRepository;

    public PredictionService(
            FastApiPredictionClient fastApiPredictionClient,
            StudentRepository studentRepository
    ) {
        this.fastApiPredictionClient = fastApiPredictionClient;
        this.studentRepository = studentRepository;
    }

    public PredictionResponse predict(StudentRequest features) {
        return fastApiPredictionClient.predict(features);
    }

    @Transactional(readOnly = true)
    public PredictionResponse predictSavedStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        return fastApiPredictionClient.predict(toFeatureRequest(student));
    }

    @Transactional(readOnly = true)
    public Map<Long, PredictionResponse> predictAllSavedStudents() {
        List<Student> students = studentRepository.findAll();
        Map<Long, PredictionResponse> predictions = new LinkedHashMap<>();
        for (Student student : students) {
            try {
                predictions.put(student.getId(), fastApiPredictionClient.predict(toFeatureRequest(student)));
            } catch (Exception ignored) {
                // Skip students that fail to predict so the rest can still be returned
            }
        }
        return predictions;
    }

    private static StudentRequest toFeatureRequest(Student student) {
        Integer activeBacklogs = student.getBacklogs();
        Double resolvedCgpa = (activeBacklogs != null && activeBacklogs > 0) ? null : student.getCgpa();
        Double attendance = student.getCurrentSemesterAttendance();
        Double testScore = student.getCurrentSemesterTestScore();
        Double assignPercent = student.getAssignmentCompletionPercent();

        String assignCategory;
        if (assignPercent == null || assignPercent < 30.0) {
            assignCategory = "High";
        } else if (assignPercent < 60.0) {
            assignCategory = "Medium";
        } else {
            assignCategory = "Low";
        }

        return new StudentRequest(
                student.getName(),
                resolvedCgpa,
                activeBacklogs,
                null,
                null,
                null,
                null,
                student.getCurrentSemesterAttendance(),
                student.getCurrentSemesterTestScore(),
                student.getAssignmentCompletionPercent(),
                attendance,
                assignCategory,
                testScore
        );
    }
}
