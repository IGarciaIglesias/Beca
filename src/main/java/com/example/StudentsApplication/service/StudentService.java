package com.example.StudentsApplication.service;

import com.example.StudentsApplication.cache.StudentCache;
import com.example.StudentsApplication.model.Student;
import com.example.StudentsApplication.model.Student.Role;
import com.example.StudentsApplication.repo.StudentRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
public class StudentService {

    private final StudentRepository repo;
    private final Validator validator;
    private final StudentCache cache;

    private static final String ERR_EMAIL_IN_USE = "El correo ya está en uso";
    private static final String ERR_AGE_INT = "age debe ser un entero";
    private static final String ERR_INVALID_DATA = "Datos inválidos";
    private static final String ERR_INVALID_ROLE = "Rol inválido";

    public StudentService(StudentRepository repo, Validator validator, StudentCache cache) {
        this.repo = repo;
        this.validator = validator;
        this.cache = cache;
    }

    public void clearCache() {
        cache.clearAll();
    }

    /* =========================
       CREATE
       ========================= */
    public Student create(Student s) {
        if (repo.existsByCorreoIgnoreCase(s.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ERR_EMAIL_IN_USE);
        }

        // ✅ default role si no viene
        if (s.getRole() == null) {
            s.setRole(Role.USER);
        }

        s.setDeleted(false);

        Student saved = repo.save(s);
        cache.putById(saved);
        cache.evictAllLists();
        return saved;
    }

    /* =========================
       READ
       ========================= */
    public Student getOr404(Long id) {
        Student s = cache.getById(id);
        if (s == null) {
            s = repo.findById(id).orElseThrow(() -> notFound(id));
            if (!Boolean.TRUE.equals(s.getDeleted())) {
                cache.putById(s);
            }
        }

        if (Boolean.TRUE.equals(s.getDeleted())) {
            throw notFound(id);
        }

        return s;
    }

    @Transactional(readOnly = true)
    public List<Student> listActive() {
        var cached = cache.getAllActiveCached();
        if (cached != null) return List.copyOf(cached);

        var list = repo.findByDeletedFalse();
        cache.putAllActive(list);
        return list;
    }

    /* =========================
       PUT (replace)
       ========================= */
    public Student replace(Long id, Student incoming) {
        Student existing = getOr404(id);

        if (!equalsIgnoreCaseSafe(existing.getCorreo(), incoming.getCorreo())
                && repo.existsByCorreoIgnoreCase(incoming.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ERR_EMAIL_IN_USE);
        }

        existing.setName(incoming.getName());
        existing.setAge(incoming.getAge());
        existing.setCorreo(incoming.getCorreo());

        // ✅ Si no viene role en PUT, mantenemos el actual (para no romper clientes)
        if (incoming.getRole() != null) {
            existing.setRole(incoming.getRole());
        }

        Student saved = repo.save(existing);
        cache.putById(saved);
        cache.evictAllLists();
        return saved;
    }

    /* =========================
       PATCH
       ========================= */
    public Student patch(Long id, Map<String, Object> fields) {

        Student existing = repo.findById(id).orElseThrow(() -> notFound(id));

        Boolean requestedDeleted = parseDeleted(fields);
        boolean restoreOnly = isRestoreOnly(fields, requestedDeleted);

        if (Boolean.TRUE.equals(existing.getDeleted()) && !restoreOnly) {
            throw notFound(id);
        }

        applyDeleted(existing, requestedDeleted);

        // si sigue activo, aplicar cambios
        if (!Boolean.TRUE.equals(existing.getDeleted())) {
            applyName(existing, fields);
            applyAge(existing, fields);
            applyCorreo(existing, fields);
            applyRole(existing, fields);     // ✅ role en PATCH
            validateOr400(existing);
        }

        Student saved = repo.save(existing);
        syncCacheAfterSave(saved);
        return saved;
    }

    /* =========================
       DELETE
       ========================= */
    public void delete(Long id, boolean soft) {
        Student s = repo.findById(id).orElseThrow(() -> notFound(id));

        if (soft) {
            if (!Boolean.TRUE.equals(s.getDeleted())) {
                s.setDeleted(true);
                repo.save(s);
            }
        } else {
            repo.deleteById(id);
        }

        cache.evictById(id);
        cache.evictAllLists();
    }

    public void delete(Long id) {
        delete(id, true);
    }

    /* =========================
       HELPERS
       ========================= */

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Estudiante " + id + " no encontrado");
    }

    private Boolean parseDeleted(Map<String, Object> fields) {
        if (!fields.containsKey("deleted")) return null;
        Object v = fields.get("deleted");
        if (v == null) return null;

        if (v instanceof Boolean b) return b;

        String s = v.toString().trim();
        if ("false".equalsIgnoreCase(s) || "0".equals(s)) return false;
        if ("true".equalsIgnoreCase(s) || "1".equals(s)) return true;

        return true;
    }

    private boolean isRestoreOnly(Map<String, Object> fields, Boolean requestedDeleted) {
        return fields.size() == 1 && Boolean.FALSE.equals(requestedDeleted);
    }

    private void applyDeleted(Student existing, Boolean requestedDeleted) {
        if (requestedDeleted != null) {
            existing.setDeleted(requestedDeleted);
        }
    }

    private void applyName(Student existing, Map<String, Object> fields) {
        if (!fields.containsKey("name")) return;
        Object v = fields.get("name");
        existing.setName(v == null ? null : v.toString());
    }

    private void applyAge(Student existing, Map<String, Object> fields) {
        if (!fields.containsKey("age")) return;

        Object v = fields.get("age");
        if (v == null) {
            existing.setAge(null);
            return;
        }

        try {
            existing.setAge(Integer.valueOf(v.toString()));
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ERR_AGE_INT);
        }
    }

    private void applyCorreo(Student existing, Map<String, Object> fields) {
        if (!fields.containsKey("correo")) return;

        Object v = fields.get("correo");
        String nuevoCorreo = (v == null ? null : v.toString());

        boolean changed = nuevoCorreo != null && !equalsIgnoreCaseSafe(existing.getCorreo(), nuevoCorreo);
        if (changed && repo.existsByCorreoIgnoreCase(nuevoCorreo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ERR_EMAIL_IN_USE);
        }

        existing.setCorreo(nuevoCorreo);
    }

    /**
     * ✅ ROLE en PATCH:
     * Espera "role": "ADMIN" o "USER"
     */
    private void applyRole(Student existing, Map<String, Object> fields) {
        if (!fields.containsKey("role")) return;

        Object v = fields.get("role");
        if (v == null) return;

        String raw = v.toString().trim().toUpperCase();
        try {
            existing.setRole(Role.valueOf(raw));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ERR_INVALID_ROLE);
        }
    }

    private void validateOr400(Student existing) {
        Set<ConstraintViolation<Student>> violations = validator.validate(existing);
        if (violations.isEmpty()) return;

        String msg = violations.stream()
                .map(ConstraintViolation::getMessage)
                .reduce((a, b) -> a + "; " + b)
                .orElse(ERR_INVALID_DATA);

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }

    private void syncCacheAfterSave(Student saved) {
        if (Boolean.TRUE.equals(saved.getDeleted())) {
            cache.evictById(saved.getId());
        } else {
            cache.putById(saved);
        }
        cache.evictAllLists();
    }

    private boolean equalsIgnoreCaseSafe(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equalsIgnoreCase(b);
    }
}