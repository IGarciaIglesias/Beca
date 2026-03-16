package com.example.StudentsApplication.service;

import com.example.StudentsApplication.cache.StudentCache;
import com.example.StudentsApplication.model.Student;
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

    // Mensajes (evita duplicación y mejora mantenibilidad)
    private static final String ERR_EMAIL_IN_USE = "El correo ya está en uso";
    private static final String ERR_AGE_INT = "age debe ser un entero";
    private static final String ERR_INVALID_DATA = "Datos inválidos";

    public StudentService(StudentRepository repo, Validator validator, StudentCache cache) {
        this.repo = repo;
        this.validator = validator;
        this.cache = cache;
    }

    public void clearCache() {
        cache.clearAll(); // vacía IMap "students" y "students_list"
    }

    public Student create(Student s) {
        if (repo.existsByCorreoIgnoreCase(s.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ERR_EMAIL_IN_USE);
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
            s = repo.findById(id).orElseThrow(() -> notFound(id));

            // 3) si no está borrado -> poner en caché
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
        if (cached != null) {
            return List.copyOf(cached);
        }
        var list = repo.findByDeletedFalse();
        cache.putAllActive(list);
        return list;
    }

    public Student replace(Long id, Student incoming) {
        Student existing = getOr404(id); // ya 404 si está borrado

        if (!equalsIgnoreCaseSafe(existing.getCorreo(), incoming.getCorreo())
                && repo.existsByCorreoIgnoreCase(incoming.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ERR_EMAIL_IN_USE);
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

    /**
     * PATCH parcial con soporte de restauración (deleted=false como único campo).
     * Refactorizado para reducir Cognitive Complexity (Sonar).
     */
    public Student patch(Long id, Map<String, Object> fields) {

        // 1) Cargar sin getOr404 para poder restaurar borrados lógicos
        Student existing = repo.findById(id).orElseThrow(() -> notFound(id));

        // 2) Parsear 'deleted' (si viene)
        Boolean requestedDeleted = parseDeleted(fields);

        // 3) ¿Es una restauración “pura”? (solo deleted=false)
        boolean restoreOnly = isRestoreOnly(fields, requestedDeleted);

        // 4) Si está borrado y NO es restauración -> 404
        if (Boolean.TRUE.equals(existing.getDeleted()) && !restoreOnly) {
            throw notFound(id);
        }

        // 5) Aplicar deleted primero (si viene)
        applyDeleted(existing, requestedDeleted);

        // Si queda borrado después de aplicar deleted, no permitimos tocar nada más
        if (!Boolean.TRUE.equals(existing.getDeleted())) {
            applyName(existing, fields);
            applyAge(existing, fields);
            applyCorreo(existing, fields);
            validateOr400(existing);
        }

        // 6) Guardar y sincronizar caché
        Student saved = repo.save(existing);
        syncCacheAfterSave(saved);
        return saved;
    }

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

        // coherencia de caché
        cache.evictById(id);
        cache.evictAllLists();
    }

    // Conserva tu delete(id) antiguo para compatibilidad si lo llama el controller actual:
    public void delete(Long id) {
        delete(id, true); // por defecto soft
    }

    // =========================
    // Helpers (refactor Sonar)
    // =========================

    private ResponseStatusException notFound(Long id) {
        return new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Estudiante " + id + " no encontrado"
        );
    }

    /**
     * Devuelve:
     *  - null si no viene "deleted" o viene null
     *  - true/false si viene boolean o string/num interpretable
     * Mantiene una semántica compatible con tu implementación original:
     *  - "false"/"0"/Boolean.FALSE => false
     *  - "true"/"1"/Boolean.TRUE  => true
     *  - valores raros => true
     */
    private Boolean parseDeleted(Map<String, Object> fields) {
        if (!fields.containsKey("deleted")) return boolean;

        Object v = fields.get("deleted");
        if (v == null) return null;

        if (v instanceof Boolean b) {
            return b;
        }

        String s = v.toString().trim();
        if ("false".equalsIgnoreCase(s) || "0".equals(s)) return false;
        if ("true".equalsIgnoreCase(s) || "1".equals(s)) return true;

        // compatible con tu lógica previa: si no es "false", tratamos como true
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

        // Solo validamos conflicto si realmente cambia el correo
        String current = existing.getCorreo();
        boolean changed = (nuevoCorreo != null)
                && (current == null || !current.equalsIgnoreCase(nuevoCorreo));

        if (changed && repo.existsByCorreoIgnoreCase(nuevoCorreo)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, ERR_EMAIL_IN_USE);
        }

        existing.setCorreo(nuevoCorreo);
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