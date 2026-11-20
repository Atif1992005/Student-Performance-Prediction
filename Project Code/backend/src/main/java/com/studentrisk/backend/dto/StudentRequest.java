package com.studentrisk.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StudentRequest(
        @JsonProperty("Name")
        String name,
        @JsonProperty("CGPA")
        @DecimalMin("0.0") @DecimalMax("10.0") Double cgpa,
        @JsonProperty("Backlogs")
        @NotNull @Min(0) @Max(20) Integer backlogs,
        @JsonProperty("Previous_Semester_Internal_Marks")
        @DecimalMin("0.0") @DecimalMax("40.0") Double previousSemesterInternalMarks,
        @JsonProperty("Previous_Semester_Attendance")
        @DecimalMin("0.0") @DecimalMax("100.0") Double previousSemesterAttendance,
        @JsonProperty("Previous_Semester_Assignment_Score")
        @DecimalMin("0.0") @DecimalMax("100.0") Double previousSemesterAssignmentScore,
        @JsonProperty("Previous_Semester_Test_Score")
        @DecimalMin("0.0") @DecimalMax("100.0") Double previousSemesterTestScore,
        @JsonProperty("Current_Semester_Attendance")
        @DecimalMin("0.0") @DecimalMax("100.0") Double currentSemesterAttendance,
        @JsonProperty("Current_Semester_Test_Score")
        @DecimalMin("0.0") @DecimalMax("100.0") Double currentSemesterTestScore,
        @JsonProperty("Assignment_Completion_Percent")
        @DecimalMin("0.0") @DecimalMax("100.0") Double assignmentCompletionPercent,
        @JsonProperty("Attendance_Percent")
        Double attendancePercent,
        @JsonProperty("Assignments_Percent")
        Object assignmentsPercent,
        @JsonProperty("Average_Test_Score_Percent")
        Double averageTestScorePercent
) {
    public StudentRequest(
            Double cgpa,
            Integer backlogs,
            Double previousSemesterInternalMarks,
            Double previousSemesterAttendance,
            Double previousSemesterAssignmentScore,
            Double previousSemesterTestScore,
            Double currentSemesterAttendance,
            Double currentSemesterTestScore,
            Double assignmentCompletionPercent
    ) {
        this(
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
                null,
                null,
                null
        );
    }
}
