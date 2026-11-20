package com.studentrisk.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.studentrisk.backend.entity.Student;

import java.time.Instant;

public record StudentResponse(
        Long id,
        @JsonProperty("Name") String name,
        @JsonProperty("CGPA") Double cgpa,
        @JsonProperty("Backlogs") Integer backlogs,
        @JsonProperty("Previous_Semester_Internal_Marks") Double previousSemesterInternalMarks,
        @JsonProperty("Previous_Semester_Attendance") Double previousSemesterAttendance,
        @JsonProperty("Previous_Semester_Assignment_Score") Double previousSemesterAssignmentScore,
        @JsonProperty("Previous_Semester_Test_Score") Double previousSemesterTestScore,
        @JsonProperty("Current_Semester_Attendance") Double currentSemesterAttendance,
        @JsonProperty("Current_Semester_Test_Score") Double currentSemesterTestScore,
        @JsonProperty("Assignment_Completion_Percent") Double assignmentCompletionPercent,
        @JsonProperty("Average_Test_Score_Percent") Double averageTestScorePercent,
        Instant createdAt,
        Instant updatedAt
) {
    public StudentResponse(
            Long id,
            Double cgpa,
            Integer backlogs,
            Double previousSemesterInternalMarks,
            Double previousSemesterAttendance,
            Double previousSemesterAssignmentScore,
            Double previousSemesterTestScore,
            Double currentSemesterAttendance,
            Double currentSemesterTestScore,
            Double assignmentCompletionPercent,
            Double averageTestScorePercent,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(
                id,
                null,
                cgpa,
                backlogs,
                previousSemesterInternalMarks,
                previousSemesterAttendance,
                previousSemesterAssignmentScore,
                previousSemesterTestScore,
                currentSemesterAttendance,
                currentSemesterTestScore,
                assignmentCompletionPercent,
                averageTestScorePercent,
                createdAt,
                updatedAt
        );
    }

    public StudentResponse(
            Long id,
            Double cgpa,
            Integer backlogs,
            Double previousSemesterInternalMarks,
            Double previousSemesterAttendance,
            Double previousSemesterAssignmentScore,
            Double previousSemesterTestScore,
            Double currentSemesterAttendance,
            Double currentSemesterTestScore,
            Double assignmentCompletionPercent,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(
                id,
                null,
                cgpa,
                backlogs,
                previousSemesterInternalMarks,
                previousSemesterAttendance,
                previousSemesterAssignmentScore,
                previousSemesterTestScore,
                currentSemesterAttendance,
                currentSemesterTestScore,
                assignmentCompletionPercent,
                calculateAverageTestScore(currentSemesterTestScore, previousSemesterTestScore),
                createdAt,
                updatedAt
        );
    }

    public static StudentResponse from(Student student) {
        Integer activeBacklogs = student.getBacklogs();
        Double resolvedCgpa = (activeBacklogs != null && activeBacklogs > 0) ? null : student.getCgpa();
        Double avgTestScore = calculateAverageTestScore(
                student.getCurrentSemesterTestScore(),
                student.getPreviousSemesterTestScore()
        );

        return new StudentResponse(
                student.getId(),
                student.getName(),
                resolvedCgpa,
                activeBacklogs,
                student.getPreviousSemesterInternalMarks(),
                student.getPreviousSemesterAttendance(),
                student.getPreviousSemesterAssignmentScore(),
                student.getPreviousSemesterTestScore(),
                student.getCurrentSemesterAttendance(),
                student.getCurrentSemesterTestScore(),
                student.getAssignmentCompletionPercent(),
                avgTestScore,
                student.getCreatedAt(),
                student.getUpdatedAt()
        );
    }

    private static Double calculateAverageTestScore(Double current, Double prev) {
        if (current == null && prev == null) return null;
        if (current == null) return prev;
        if (prev == null) return current;
        return Math.round(((current + prev) / 2.0) * 100.0) / 100.0;
    }
}
