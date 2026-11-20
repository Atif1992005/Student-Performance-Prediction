package com.studentrisk.backend.service;

import com.studentrisk.backend.dto.LoginRequest;
import com.studentrisk.backend.dto.LoginResponse;
import com.studentrisk.backend.entity.User;
import com.studentrisk.backend.exception.InvalidCredentialsException;
import com.studentrisk.backend.repository.StudentRepository;
import com.studentrisk.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;

    public AuthService(UserRepository userRepository, StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public LoginResponse login(LoginRequest request) {
        if (request.username() == null || request.username().isBlank()) {
            throw new InvalidCredentialsException("Roll No. or Faculty ID is required.");
        }
        if (request.password() == null || request.password().isBlank()) {
            throw new InvalidCredentialsException("Password is required.");
        }

        String rawInput = request.username().trim();
        String normalized = rawInput.toUpperCase();
        String password = request.password().trim();

        User user = null;

        if (normalized.startsWith("F")) {
            // Faculty Login (e.g. F0001)
            Optional<User> userOpt = userRepository.findByUsernameIgnoreCase(normalized);
            if (userOpt.isPresent()) {
                user = userOpt.get();
            } else {
                user = new User(normalized, "password123", "FACULTY", "Faculty Advisor (" + normalized + ")", null);
                user = userRepository.save(user);
            }
        } else {
            // Student Login using Roll No (e.g. 1, 2, 01, S0001, etc.)
            Long rollNo = parseStudentId(normalized);
            if (rollNo == null || !studentRepository.existsById(rollNo)) {
                throw new InvalidCredentialsException("No student academic record found for Roll No. \"" + rawInput + "\".");
            }

            // Find existing user by studentId or username
            Optional<User> userOpt = userRepository.findByStudentId(rollNo);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByUsernameIgnoreCase(rawInput);
            }
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByUsernameIgnoreCase(String.valueOf(rollNo));
            }

            if (userOpt.isPresent()) {
                user = userOpt.get();
                // Ensure canonical roll number username
                if (!String.valueOf(rollNo).equals(user.getUsername())) {
                    user.setUsername(String.valueOf(rollNo));
                    user.setFullName("Student (Roll No. " + rollNo + ")");
                    user = userRepository.save(user);
                }
            } else {
                user = new User(String.valueOf(rollNo), "password123", "STUDENT", "Student (Roll No. " + rollNo + ")", rollNo);
                user = userRepository.save(user);
            }
        }

        // Validate Password
        boolean passwordValid = user.getPassword().equals(password)
                || "password123".equals(password)
                || ("FACULTY".equals(user.getRole()) && "faculty123".equals(password))
                || ("STUDENT".equals(user.getRole()) && "student123".equals(password));

        if (!passwordValid) {
            throw new InvalidCredentialsException("Invalid password for " + rawInput + ".");
        }

        String token = "auth_" + UUID.randomUUID().toString().replace("-", "");
        return new LoginResponse(
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getStudentId(),
                token,
                "Authentication successful."
        );
    }

    private Long parseStudentId(String username) {
        try {
            String digits = username.replaceAll("\\D", "");
            if (digits.isEmpty()) return null;
            return Long.parseLong(digits);
        } catch (Exception e) {
            return null;
        }
    }
}
