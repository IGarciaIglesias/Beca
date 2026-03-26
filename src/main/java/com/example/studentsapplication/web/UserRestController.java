package com.example.studentsapplication.web;

import com.example.studentsapplication.model.User;
import com.example.studentsapplication.repo.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserRestController {

    private final UserRepository repo;

    public UserRestController(UserRepository repo) {
        this.repo = repo;
    }

    // LISTAR USUARIOS
    @GetMapping
    public List<User> list() {
        return repo.findAll();
    }

    // OBTENER USUARIO
    @GetMapping("/{id}")
    public User get(@PathVariable Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    // CREAR USUARIO
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@RequestBody User u) {
        return repo.save(u);
    }

    // EDITAR USUARIO
    @PutMapping("/{id}")
    public User replace(@PathVariable Long id, @RequestBody User incoming) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        incoming.setId(id);
        return repo.save(incoming);
    }

    // BORRAR USUARIO
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        repo.deleteById(id);
    }
}

