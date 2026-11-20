package com.studentrisk.backend.config;

import com.studentrisk.backend.entity.Student;
import com.studentrisk.backend.entity.User;
import com.studentrisk.backend.repository.StudentRepository;
import com.studentrisk.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public DataInitializer(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Initialize Faculty Account F0001 if missing
        if (!userRepository.existsByUsernameIgnoreCase("F0001")) {
            User faculty = new User(
                    "F0001",
                    "password123",
                    "FACULTY",
                    "Dr. Ramesh Sharma (Faculty Advisor)",
                    null
            );
            userRepository.save(faculty);
            log.info("[Auth] Created default faculty account: F0001");
        }

        // 2. Initialize Student Accounts using Roll Numbers (1, 2, 3, ...)
        List<Student> students = studentRepository.findAll();
        for (Student student : students) {
            String rollNo = String.valueOf(student.getId());
            Optional<User> existingUser = userRepository.findByStudentId(student.getId());
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (!rollNo.equals(user.getUsername())) {
                    user.setUsername(rollNo);
                    user.setFullName("Student (Roll No. " + rollNo + ")");
                    userRepository.save(user);
                }
            } else if (!userRepository.existsByUsernameIgnoreCase(rollNo)) {
                User studentUser = new User(
                        rollNo,
                        "password123",
                        "STUDENT",
                        "Student (Roll No. " + rollNo + ")",
                        student.getId()
                );
                userRepository.save(studentUser);
            }
        }
        log.info("[Auth] Synchronized roll number accounts for {} students.", students.size());
    }
}
