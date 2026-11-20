package com.studentrisk.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = true, length = 100)
    private String name;

    @Column(name = "cgpa", nullable = true)
    private Double cgpa;

    @Column(name = "backlogs", nullable = false)
    private Integer backlogs;

    @Column(name = "previous_semester_internal_marks", nullable = true)
    private Double previousSemesterInternalMarks;

    @Column(name = "previous_semester_attendance", nullable = true)
    private Double previousSemesterAttendance;

    @Column(name = "previous_semester_assignment_score", nullable = true)
    private Double previousSemesterAssignmentScore;

    @Column(name = "previous_semester_test_score", nullable = true)
    private Double previousSemesterTestScore;

    @Column(name = "current_semester_attendance", nullable = false)
    private Double currentSemesterAttendance;

    @Column(name = "current_semester_test_score", nullable = false)
    private Double currentSemesterTestScore;

    @Column(name = "assignment_completion_percent", nullable = false)
    private Double assignmentCompletionPercent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCgpa() {
        return cgpa;
    }

    public void setCgpa(Double cgpa) {
        this.cgpa = cgpa;
    }

    public Integer getBacklogs() {
        return backlogs;
    }

    public void setBacklogs(Integer backlogs) {
        this.backlogs = backlogs;
    }

    public Double getPreviousSemesterInternalMarks() {
        return previousSemesterInternalMarks;
    }

    public void setPreviousSemesterInternalMarks(Double previousSemesterInternalMarks) {
        this.previousSemesterInternalMarks = previousSemesterInternalMarks;
    }

    public Double getPreviousSemesterAttendance() {
        return previousSemesterAttendance;
    }

    public void setPreviousSemesterAttendance(Double previousSemesterAttendance) {
        this.previousSemesterAttendance = previousSemesterAttendance;
    }

    public Double getPreviousSemesterAssignmentScore() {
        return previousSemesterAssignmentScore;
    }

    public void setPreviousSemesterAssignmentScore(Double previousSemesterAssignmentScore) {
        this.previousSemesterAssignmentScore = previousSemesterAssignmentScore;
    }

    public Double getPreviousSemesterTestScore() {
        return previousSemesterTestScore;
    }

    public void setPreviousSemesterTestScore(Double previousSemesterTestScore) {
        this.previousSemesterTestScore = previousSemesterTestScore;
    }

    public Double getCurrentSemesterAttendance() {
        return currentSemesterAttendance;
    }

    public void setCurrentSemesterAttendance(Double currentSemesterAttendance) {
        this.currentSemesterAttendance = currentSemesterAttendance;
    }

    public Double getCurrentSemesterTestScore() {
        return currentSemesterTestScore;
    }

    public void setCurrentSemesterTestScore(Double currentSemesterTestScore) {
        this.currentSemesterTestScore = currentSemesterTestScore;
    }

    public Double getAssignmentCompletionPercent() {
        return assignmentCompletionPercent;
    }

    public void setAssignmentCompletionPercent(Double assignmentCompletionPercent) {
        this.assignmentCompletionPercent = assignmentCompletionPercent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
