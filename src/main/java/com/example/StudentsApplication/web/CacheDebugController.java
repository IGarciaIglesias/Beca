package com.example.StudentsApplication.web;

import com.example.StudentsApplication.cache.StudentCache;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Profile("test")
@RestController
@RequestMapping("/students/cache")
public class CacheDebugController {

    private final StudentCache cache;

    public CacheDebugController(StudentCache cache) {
        this.cache = cache;
    }

    /** ¿Está cacheado el id? */
    @GetMapping("/contains/{id}")
    public Map<String, Object> contains(@PathVariable Long id) {
        boolean present = cache.containsId(id);
        return Map.of("id", id, "present", present);
    }

    /** Estadísticas simples: tamaños y si existe la lista 'all' */
    @GetMapping("/stats")
    public Map<String, Object> stats() {
        return Map.of(
                "studentsSize", cache.sizeById(),
                "studentsListSize", cache.sizeLists(),
                "listAllPresent", cache.isListAllPresent()
        );
    }
}