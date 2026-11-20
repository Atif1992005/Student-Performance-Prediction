package com.studentrisk.backend.service;

import com.studentrisk.backend.dto.BatchImportResponse;
import com.studentrisk.backend.dto.StudentRequest;
import com.studentrisk.backend.dto.StudentResponse;
import com.studentrisk.backend.entity.Student;
import com.studentrisk.backend.entity.User;
import com.studentrisk.backend.exception.ResourceNotFoundException;
import com.studentrisk.backend.repository.StudentRepository;
import com.studentrisk.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;

    public StudentService(StudentRepository studentRepository, UserRepository userRepository) {
        this(studentRepository, userRepository, null);
    }

    @Autowired
    public StudentService(StudentRepository studentRepository, UserRepository userRepository, JdbcTemplate jdbcTemplate) {
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream().map(StudentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        return StudentResponse.from(student);
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        Student student = new Student();
        apply(student, request);
        Student saved = studentRepository.save(student);

        // Auto-provision corresponding student user account using Roll No.
        String rollNo = String.valueOf(saved.getId());
        if (!userRepository.existsByUsernameIgnoreCase(rollNo)) {
            userRepository.save(new User(
                    rollNo,
                    "password123",
                    "STUDENT",
                    saved.getName() != null && !saved.getName().isBlank() ? saved.getName() : "Student (Roll No. " + rollNo + ")",
                    saved.getId()
            ));
        }

        return StudentResponse.from(saved);
    }

    @Transactional
    public StudentResponse update(Long id, StudentRequest request) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        apply(student, request);
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional
    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Student not found: " + id);
        }
        userRepository.findByStudentId(id).ifPresent(userRepository::delete);
        studentRepository.deleteById(id);
    }

    @Transactional
    public BatchImportResponse importCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded CSV file is empty.");
        }
        if (jdbcTemplate == null) {
            throw new IllegalStateException("JdbcTemplate is not configured for batch import.");
        }

        List<String> errors = new ArrayList<>();
        int totalRows = 0;
        int processedCount = 0;
        int skippedCount = 0;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("CSV file contains no content.");
            }

            // Remove UTF-8 BOM if present
            if (headerLine.startsWith("\uFEFF")) {
                headerLine = headerLine.substring(1);
            }

            List<String> headers = parseCsvLine(headerLine);
            Map<String, Integer> colIndexMap = new HashMap<>();
            for (int i = 0; i < headers.size(); i++) {
                String cleanHeader = headers.get(i).trim().toLowerCase().replaceAll("[^a-z0-9]", "");
                if (cleanHeader.equals("rollno") || cleanHeader.equals("roll") || cleanHeader.equals("id") || cleanHeader.equals("studentid")) {
                    colIndexMap.put("id", i);
                } else if (cleanHeader.equals("studentname") || cleanHeader.equals("name") || cleanHeader.equals("fullname")) {
                    colIndexMap.put("name", i);
                } else if (cleanHeader.equals("cgpa") || cleanHeader.equals("gpa")) {
                    colIndexMap.put("cgpa", i);
                } else if (cleanHeader.contains("attendance")) {
                    colIndexMap.put("attendance", i);
                } else if (cleanHeader.contains("backlog")) {
                    colIndexMap.put("backlogs", i);
                } else if (cleanHeader.contains("assignment")) {
                    colIndexMap.put("assignment", i);
                } else if (cleanHeader.contains("test") || cleanHeader.contains("score")) {
                    colIndexMap.put("test_score", i);
                }
            }

            if (!colIndexMap.containsKey("id")) {
                throw new IllegalArgumentException("CSV missing required 'Roll_No' or 'id' column header.");
            }

            String line;
            int lineNum = 1;
            while ((line = reader.readLine()) != null) {
                lineNum++;
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                totalRows++;
                List<String> tokens = parseCsvLine(line);

                try {
                    // Extract Roll No / ID
                    int idCol = colIndexMap.get("id");
                    if (idCol >= tokens.size() || tokens.get(idCol).isBlank()) {
                        errors.add("Line " + lineNum + ": Missing Roll No / ID; skipped.");
                        skippedCount++;
                        continue;
                    }
                    long rollNo = Long.parseLong(tokens.get(idCol).trim());

                    // Extract Name
                    String name = "Student " + rollNo;
                    if (colIndexMap.containsKey("name")) {
                        int nameCol = colIndexMap.get("name");
                        if (nameCol < tokens.size() && !tokens.get(nameCol).isBlank()) {
                            name = tokens.get(nameCol).trim();
                        }
                    }

                    // Extract Backlogs
                    int backlogs = 0;
                    if (colIndexMap.containsKey("backlogs")) {
                        int bCol = colIndexMap.get("backlogs");
                        if (bCol < tokens.size() && !tokens.get(bCol).isBlank()) {
                            backlogs = Integer.parseInt(tokens.get(bCol).trim());
                        }
                    }

                    // Extract CGPA (blank or backlogs > 0 => NULL)
                    Double cgpa = null;
                    if (backlogs == 0 && colIndexMap.containsKey("cgpa")) {
                        int cCol = colIndexMap.get("cgpa");
                        if (cCol < tokens.size() && !tokens.get(cCol).isBlank()) {
                            cgpa = Double.parseDouble(tokens.get(cCol).trim());
                        }
                    }

                    // Extract Attendance
                    double attendance = 75.0;
                    if (colIndexMap.containsKey("attendance")) {
                        int aCol = colIndexMap.get("attendance");
                        if (aCol < tokens.size() && !tokens.get(aCol).isBlank()) {
                            attendance = Double.parseDouble(tokens.get(aCol).trim());
                        }
                    }

                    // Extract Assignments
                    double assignments = 70.0;
                    if (colIndexMap.containsKey("assignment")) {
                        int asCol = colIndexMap.get("assignment");
                        if (asCol < tokens.size() && !tokens.get(asCol).isBlank()) {
                            assignments = Double.parseDouble(tokens.get(asCol).trim());
                        }
                    }

                    // Extract Test Score
                    double testScore = 60.0;
                    if (colIndexMap.containsKey("test_score")) {
                        int tCol = colIndexMap.get("test_score");
                        if (tCol < tokens.size() && !tokens.get(tCol).isBlank()) {
                            testScore = Double.parseDouble(tokens.get(tCol).trim());
                        }
                    }

                    // Upsert into students table (prevents duplicates)
                    String studentSql = """
                            INSERT INTO students (id, name, cgpa, backlogs, current_semester_attendance, current_semester_test_score, assignment_completion_percent, created_at, updated_at)
                            VALUES (?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
                            ON DUPLICATE KEY UPDATE
                                name = VALUES(name),
                                cgpa = VALUES(cgpa),
                                backlogs = VALUES(backlogs),
                                current_semester_attendance = VALUES(current_semester_attendance),
                                current_semester_test_score = VALUES(current_semester_test_score),
                                assignment_completion_percent = VALUES(assignment_completion_percent),
                                updated_at = NOW()
                            """;
                    jdbcTemplate.update(studentSql, rollNo, name, cgpa, backlogs, attendance, testScore, assignments);

                    // Upsert into app_users table (preserves/links account)
                    String userSql = """
                            INSERT INTO app_users (username, password, role, full_name, student_id, created_at, updated_at)
                            VALUES (?, 'password123', 'STUDENT', ?, ?, NOW(), NOW())
                            ON DUPLICATE KEY UPDATE
                                full_name = VALUES(full_name),
                                student_id = VALUES(student_id),
                                updated_at = NOW()
                            """;
                    jdbcTemplate.update(userSql, String.valueOf(rollNo), name, rollNo);

                    processedCount++;
                } catch (Exception ex) {
                    errors.add("Line " + lineNum + ": " + ex.getMessage());
                    skippedCount++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV input stream: " + e.getMessage(), e);
        }

        return BatchImportResponse.of(
                errors.isEmpty(),
                "Successfully imported " + processedCount + " student records (" + skippedCount + " skipped).",
                totalRows,
                processedCount,
                skippedCount,
                errors
        );
    }

    private List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim());
        return tokens;
    }

    private void apply(Student student, StudentRequest request) {
        if (request.name() != null) {
            student.setName(request.name().trim());
        }

        // If Backlogs > 0, CGPA must be NULL/N/A (not 0)
        Double resolvedCgpa = (request.backlogs() != null && request.backlogs() > 0) ? null : request.cgpa();
        student.setCgpa(resolvedCgpa);

        student.setBacklogs(request.backlogs());
        student.setPreviousSemesterInternalMarks(request.previousSemesterInternalMarks());
        student.setPreviousSemesterAttendance(request.previousSemesterAttendance());
        student.setPreviousSemesterAssignmentScore(request.previousSemesterAssignmentScore());
        student.setPreviousSemesterTestScore(request.previousSemesterTestScore());
        student.setCurrentSemesterAttendance(request.currentSemesterAttendance());
        student.setCurrentSemesterTestScore(request.currentSemesterTestScore());
        student.setAssignmentCompletionPercent(request.assignmentCompletionPercent());
    }
}
