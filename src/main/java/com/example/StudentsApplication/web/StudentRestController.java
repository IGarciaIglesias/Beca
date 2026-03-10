package com.example.StudentsApplication.web;

import com.example.StudentsApplication.model.Student;
import com.example.StudentsApplication.repo.StudentRepository;
import com.example.StudentsApplication.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

@RestController
@RequestMapping("/students")

/*
*
* Clase para el manejo de los endpoints, los inicializo y gestiono
*
*/

public class StudentRestController {

    private final StudentRepository repo;
    private final StudentService service;

    public StudentRestController(StudentRepository repo, StudentService service) {
        this.repo = repo;
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Student create(@Valid @RequestBody Student s) {
        return service.create(s);
    }

    @GetMapping
    public List<Student> list() {
        return service.listActive(); // <--- aprovecha caché
    }

    @GetMapping("/{id}")
    public Student get(@PathVariable Long id) {
        return service.getOr404(id); // <--- 404 si borrado
    }

    @PutMapping("/{id}")
    public Student replace(@PathVariable Long id, @Valid @RequestBody Student incoming) {
        return service.replace(id, incoming);
    }

    @PatchMapping("/{id}")
    public Student patch(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        return service.patch(id, fields);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestParam(defaultValue = "true") boolean soft) {
        service.delete(id, soft); // <--- soft por defecto
    }

    @GetMapping("/refresh")
    public Map<String, Object> refresh() {
        service.clearCache();
        return Map.of(
                "status", "ok",
                "cleared", java.util.List.of("students", "students_list")
        );
    }
}
