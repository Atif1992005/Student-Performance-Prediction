package com.studentrisk.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studentrisk.backend.dto.StudentRequest;
import com.studentrisk.backend.dto.StudentResponse;
import com.studentrisk.backend.exception.ResourceNotFoundException;
import com.studentrisk.backend.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private StudentService studentService;

    private StudentRequest sampleRequest() {
        return new StudentRequest(
                7.35, 2, 19.6, 66.3,
                54.7, 62.1, 61.9, 41.0, 20.0
        );
    }

    private StudentResponse sampleResponse(Long id) {
        return new StudentResponse(
                id, 7.35, 2, 19.6, 66.3,
                54.7, 62.1, 61.9, 41.0, 20.0,
                Instant.now(), Instant.now()
        );
    }

    @Test
    void listStudents_ReturnsList() throws Exception {
        when(studentService.findAll()).thenReturn(List.of(sampleResponse(1L)));

        mockMvc.perform(get("/api/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].CGPA").value(7.35));
    }

    @Test
    void getById_Found_ReturnsStudent() throws Exception {
        when(studentService.findById(1L)).thenReturn(sampleResponse(1L));

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.Backlogs").value(2));
    }

    @Test
    void getById_NotFound_Returns404() throws Exception {
        when(studentService.findById(99L)).thenThrow(new ResourceNotFoundException("Student not found: 99"));

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }

    @Test
    void createStudent_ValidPayload_Returns201() throws Exception {
        StudentRequest req = sampleRequest();
        when(studentService.create(any(StudentRequest.class))).thenReturn(sampleResponse(1L));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createStudent_WithoutCgpa_Returns201() throws Exception {
        StudentRequest reqWithoutCgpa = new StudentRequest(
                null, 2, 19.6, 66.3,
                54.7, 62.1, 61.9, 41.0, 20.0
        );
        StudentResponse resWithoutCgpa = new StudentResponse(
                2L, null, 2, 19.6, 66.3,
                54.7, 62.1, 61.9, 41.0, 20.0,
                Instant.now(), Instant.now()
        );
        when(studentService.create(any(StudentRequest.class))).thenReturn(resWithoutCgpa);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqWithoutCgpa)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.CGPA").doesNotExist());
    }

    @Test
    void updateStudent_ValidPayload_Returns200() throws Exception {
        StudentRequest req = sampleRequest();
        when(studentService.update(eq(1L), any(StudentRequest.class))).thenReturn(sampleResponse(1L));

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteStudent_Found_Returns204() throws Exception {
        doNothing().when(studentService).delete(1L);

        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteStudent_NotFound_Returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Student not found: 99")).when(studentService).delete(99L);

        mockMvc.perform(delete("/api/students/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadCsv_Success_Returns200() throws Exception {
        org.springframework.mock.web.MockMultipartFile file = new org.springframework.mock.web.MockMultipartFile(
                "file",
                "students.csv",
                "text/csv",
                "Roll_No,Student_Name,CGPA,Attendance_Percent,Backlogs,Assignment_Completion_Percent,Average_Test_Score_Percent\n101,Rutuja,7.07,74,0,72,60.67".getBytes()
        );

        when(studentService.importCsv(any())).thenReturn(
                com.studentrisk.backend.dto.BatchImportResponse.of(true, "Successfully imported 1 student records.", 1, 1, 0, java.util.List.of())
        );

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart("/api/students/upload-csv")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalRows").value(1));
    }
}
