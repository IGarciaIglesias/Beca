package com.example.studentsapplication.web;

import com.example.studentsapplication.model.Profesor;
import com.example.studentsapplication.repo.ProfesorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/profesores")
public class ProfesorRestController {

    private final ProfesorRepository repo;

    public ProfesorRestController(ProfesorRepository repo) {
        this.repo = repo;
    }

    // LISTAR (todos)
    @GetMapping
    public List<Profesor> list() {
        return repo.findAll();
    }

    // OBTENER POR ID
    @GetMapping("/{id}")
    public Profesor get(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // CREAR
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Profesor create(@RequestBody Profesor p) {
        return repo.save(p);
    }

    // EDITAR (PUT completo)
    @PutMapping("/{id}")
    public Profesor replace(@PathVariable Long id, @RequestBody Profesor incoming) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        incoming.setId(id);
        return repo.save(incoming);
    }

    // BORRAR
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        repo.deleteById(id);
    }
}
