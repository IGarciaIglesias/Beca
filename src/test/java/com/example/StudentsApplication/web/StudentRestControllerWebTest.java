package com.example.StudentsApplication.web;

import com.example.StudentsApplication.model.Student;
import com.example.StudentsApplication.repo.StudentRepository;
import com.example.StudentsApplication.service.StudentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test MVC (sin levantar servidor real) para subir coverage de:
 * - StudentRestController
 * - RestExceptionHandler (validación 400)
 */
@WebMvcTest(controllers = StudentRestController.class)
@Import(RestExceptionHandler.class)
class StudentRestControllerWebTest {

    @Autowired
    private MockMvc mvc;

    // StudentRestController recibe repo y service por constructor, así que hay que mockear ambos
    @MockBean
    private StudentRepository repo;

    @MockBean
    private StudentService service;

    private Student s(long id) {
        Student st = new Student();
        st.setId(id);
        st.setName("Ana");
        st.setAge(20);
        st.setCorreo("ana@uni.es");
        st.setDeleted(false);
        return st;
    }

    // ------------------------
    // GET /students
    // ------------------------
    @Test
    void list_students_200() throws Exception {
        when(service.listActive()).thenReturn(List.of(s(1)));

        mvc.perform(get("/students"))
                .andExpect(status().isOk());
    }

    // ------------------------
    // GET /students/{id}
    // ------------------------
    @Test
    void get_student_200() throws Exception {
        when(service.getOr404(1L)).thenReturn(s(1));

        mvc.perform(get("/students/1"))
                .andExpect(status().isOk());
    }

    @Test
    void get_student_404_whenServiceThrows() throws Exception {
        when(service.getOr404(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "no"));

        mvc.perform(get("/students/999"))
                .andExpect(status().isNotFound());
    }

    // ------------------------
    // POST /students  (201 + 400 por @Valid)
    // ------------------------
    @Test
    void create_student_201() throws Exception {
        when(service.create(any(Student.class))).thenReturn(s(10));

        mvc.perform(post("/students")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name":"Ana",
                                  "age":20,
                                  "correo":"ana@uni.es"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    /**
     * Fuerza MethodArgumentNotValidException -> RestExceptionHandler -> 400 con body {status, errors}
     * Nota: esto depende de que tu entidad Student tenga validaciones (@NotBlank, @Email, etc.)
     */
    @Test
    void create_student_400_triggersRestExceptionHandler() throws Exception {
        mvc.perform(post("/students")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name":" ",
                                  "age":-1,
                                  "correo":"no-es-email"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").exists());
    }

    // ------------------------
    // PUT /students/{id}
    // ------------------------
    @Test
    void replace_student_200() throws Exception {
        when(service.replace(eq(1L), any(Student.class))).thenReturn(s(1));

        mvc.perform(put("/students/1")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name":"Ana Mod",
                                  "age":21,
                                  "correo":"ana2@uni.es"
                                }
                                """))
                .andExpect(status().isOk());
    }

    // ------------------------
    // PATCH /students/{id}
    // ------------------------
    @Test
    void patch_student_200() throws Exception {
        when(service.patch(eq(1L), any(Map.class))).thenReturn(s(1));

        mvc.perform(patch("/students/1")
                        .contentType("application/json")
                        .content("{\"age\":22}"))
                .andExpect(status().isOk());
    }

    // ------------------------
    // DELETE /students/{id}?soft=...
    // ------------------------
    @Test
    void delete_student_softDefault_204() throws Exception {
        mvc.perform(delete("/students/1"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L, true);
    }

    @Test
    void delete_student_hard_204() throws Exception {
        mvc.perform(delete("/students/1").param("soft", "false"))
                .andExpect(status().isNoContent());

        verify(service).delete(1L, false);
    }

    // ------------------------
    // GET /students/refresh
    // ------------------------
    @Test
    void refresh_clearsCache_andReturnsOk() throws Exception {
        mvc.perform(get("/students/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"))
                .andExpect(jsonPath("$.cleared").exists());

        verify(service).clearCache();
    }
}