package com.example.StudentsApplication.service;

import com.example.StudentsApplication.model.Student;
import com.example.StudentsApplication.repo.StudentRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.example.StudentsApplication.cache.StudentCache;

import java.util.Map;
import java.util.Set;
import java.util.List;


@Service
@Transactional
public class StudentService {

    private final StudentRepository repo;
    private final Validator validator;
    private final StudentCache cache;

    public StudentService(StudentRepository repo, Validator validator, StudentCache cache) {
            this.repo = repo;
            this.validator = validator;
            this.cache = cache;
          }

    // StudentService.java
    public void clearCache() {
        cache.clearAll(); // <- vacía IMap "students" y "students_list"
    }

    public Student create(Student s) {
        if (repo.existsByCorreoIgnoreCase(s.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está en uso");
        }
        s.setDeleted(false); // asegurar no borrado al crear

        Student saved = repo.save(s);
        // actualizar caché: por id y listas
        cache.putById(saved);
        cache.evictAllLists();
        return saved;
    }

    public Student getOr404(Long id) {

        // 1) buscar en caché primero
        Student s = cache.getById(id);
        if (s == null) {
            // 2) no está en caché -> cargar de BBDD
                    s = repo.findById(id).orElseThrow(
                    () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante " + id + " no encontrado"));
            // 3) si no está borrado -> poner en caché
            if (!Boolean.TRUE.equals(s.getDeleted())) {
                cache.putById(s);
            }
        }
        if (Boolean.TRUE.equals(s.getDeleted())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante " + id + " no encontrado");
        }
        return s;
    }


    // Listado de no borrados, con caché opcional (clave 'all')
    @Transactional(readOnly = true)
    public List<Student> listActive() {
        var cached = cache.getAllActiveCached();
        if (cached != null) return List.copyOf(cached);
        var list = repo.findByDeletedFalse();
        cache.putAllActive(list);
        return list;
    }

    public Student replace(Long id, Student incoming) {
        Student existing = getOr404(id); // ya 404 si está borrado
        if (!existing.getCorreo().equalsIgnoreCase(incoming.getCorreo())
                && repo.existsByCorreoIgnoreCase(incoming.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está en uso");
        }
        existing.setName(incoming.getName());
        existing.setAge(incoming.getAge());
        existing.setCorreo(incoming.getCorreo());
        // mantener su estado deleted

        Student saved = repo.save(existing);
        cache.putById(saved);
        cache.evictAllLists();
        return saved;
    }

    public Student patch(Long id, Map<String, Object> fields) {
        // 1) Cargar sin getOr404 para poder restaurar borrados lógicos
        Student existing = repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante " + id + " no encontrado"));

        // 2) Es una restauración? (único campo: deleted=false)
        boolean hasDeleted = fields.containsKey("deleted");
        boolean deletedValueIsFalse = false;
        if (hasDeleted) {
            Object v = fields.get("deleted");
            if (v != null) {
                String s = v.toString().trim();
                deletedValueIsFalse = "false".equalsIgnoreCase(s) || "0".equals(s);
                // también aceptamos Boolean.FALSE
                if (v instanceof Boolean b) deletedValueIsFalse = !b ? true : deletedValueIsFalse;
            } else {
                // "deleted": null - no lo tratamos como restauración
                deletedValueIsFalse = false;
            }
        }
        boolean restoreOnly = (fields.size() == 1) && hasDeleted && deletedValueIsFalse;

        // 3) Si está borrado y NO es restauración - 404
        if (Boolean.TRUE.equals(existing.getDeleted()) && !restoreOnly) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante " + id + " no encontrado");
        }

        // 4) Aplicar primero 'deleted' si viene
        if (hasDeleted) {
            existing.setDeleted(!deletedValueIsFalse ? true : false);
        }

        // Si sigue borrado después de aplicar 'deleted', no permitimos tocar nada más
        if (Boolean.TRUE.equals(existing.getDeleted())) {
            return repo.save(existing);
        }

        // 5) Ya está activo (deleted=false): aplicar el resto de campos
        if (fields.containsKey("name")) {
            Object v = fields.get("name");
            existing.setName(v == null ? null : v.toString());
        }
        if (fields.containsKey("age")) {
            Object v = fields.get("age");
            if (v != null) {
                try { existing.setAge(Integer.valueOf(v.toString())); }
                catch (NumberFormatException e) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "age debe ser un entero");
                }
            } else {
                existing.setAge(null);
            }
        }
        if (fields.containsKey("correo")) {
            Object v = fields.get("correo");
            String nuevoCorreo = v == null ? null : v.toString();
            if (nuevoCorreo != null &&
                    !existing.getCorreo().equalsIgnoreCase(nuevoCorreo) &&
                    repo.existsByCorreoIgnoreCase(nuevoCorreo)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo ya está en uso");
            }
            existing.setCorreo(nuevoCorreo);
        }

        // 6) Validación final
        Set<ConstraintViolation<Student>> violations = validator.validate(existing);
        if (!violations.isEmpty()) {
            String msg = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Datos inválidos");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
        }


        Student saved = repo.save(existing);
        // Si quedó marcado como borrado => sacar del caché por id.
        if (Boolean.TRUE.equals(saved.getDeleted())) {
            cache.evictById(saved.getId());
        } else {
            cache.putById(saved);
        }
        cache.evictAllLists();
        return saved;
    }

    public void delete(Long id, boolean soft) {
        Student s = repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante " + id + " no encontrado"));
        if (soft) {
            if (!Boolean.TRUE.equals(s.getDeleted())) {
                s.setDeleted(true);
                repo.save(s);
            }
        } else {
            repo.deleteById(id);
        }
        // coherencia de caché
        cache.evictById(id);
        cache.evictAllLists();
    }

    // Conserva tu delete(id) antiguo para compatibilidad si lo llama el controller actual:
    public void delete(Long id) {
        delete(id, true); // por defecto soft
    }
}