package com.example.StudentsApplication.service;

import com.example.StudentsApplication.cache.StudentCache;
import com.example.StudentsApplication.model.Student;
import com.example.StudentsApplication.repo.StudentRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock private StudentRepository repo;
    @Mock private Validator validator;
    @Mock private StudentCache cache;

    private StudentService service;

    @BeforeEach
    void setUp() {
        service = new StudentService(repo, validator, cache);
        // IMPORTANTE: NO stubbear validator aquí.
        // Se stubbeará solo en los tests que realmente validen.
    }

    // ------------------------
    // Helpers
    // ------------------------
    private Student student(Long id, String name, Integer age, String correo, Boolean deleted) {
        Student s = new Student();
        s.setId(id);
        s.setName(name);
        s.setAge(age);
        s.setCorreo(correo);
        s.setDeleted(deleted);
        return s;
    }

    private static void assertStatus(ResponseStatusException ex, HttpStatus status) {
        assertEquals(status, ex.getStatusCode());
    }

    // ------------------------
    // create()
    // ------------------------

    @Test
    void create_whenEmailAlreadyExists_throws409() {
        Student incoming = student(null, "Ana", 20, "ana@uni.es", null);
        when(repo.existsByCorreoIgnoreCase("ana@uni.es")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.create(incoming));
        assertStatus(ex, HttpStatus.CONFLICT);

        verify(repo, never()).save(any());
        verify(cache, never()).putById(any());
        verify(cache, never()).evictAllLists();
    }

    @Test
    void create_setsDeletedFalse_saves_and_updatesCache() {
        Student incoming = student(null, "Ana", 20, "ana@uni.es", true);
        Student saved = student(10L, "Ana", 20, "ana@uni.es", false);

        when(repo.existsByCorreoIgnoreCase("ana@uni.es")).thenReturn(false);
        when(repo.save(any(Student.class))).thenReturn(saved);

        Student result = service.create(incoming);

        assertEquals(10L, result.getId());
        assertFalse(Boolean.TRUE.equals(incoming.getDeleted()), "Al crear debe quedar deleted=false");

        verify(cache).putById(saved);
        verify(cache).evictAllLists();
    }

    // ------------------------
    // getOr404()
    // ------------------------

    @Test
    void getOr404_returnsFromCache_whenPresentAndActive() {
        Student cached = student(1L, "Ana", 20, "a@uni.es", false);
        when(cache.getById(1L)).thenReturn(cached);

        Student result = service.getOr404(1L);

        assertSame(cached, result);
        verify(repo, never()).findById(anyLong());
    }

    @Test
    void getOr404_loadsFromRepo_andCaches_whenActiveAndNotInCache() {
        when(cache.getById(1L)).thenReturn(null);
        Student db = student(1L, "Ana", 20, "a@uni.es", false);
        when(repo.findById(1L)).thenReturn(Optional.of(db));

        Student result = service.getOr404(1L);

        assertSame(db, result);
        verify(cache).putById(db);
    }

    @Test
    void getOr404_throws404_whenCachedButDeleted() {
        Student cachedDeleted = student(1L, "Ana", 20, "a@uni.es", true);
        when(cache.getById(1L)).thenReturn(cachedDeleted);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getOr404(1L));
        assertStatus(ex, HttpStatus.NOT_FOUND);

        verify(repo, never()).findById(anyLong());
    }

    @Test
    void getOr404_throws404_whenRepoReturnsDeleted() {
        when(cache.getById(1L)).thenReturn(null);
        Student dbDeleted = student(1L, "Ana", 20, "a@uni.es", true);
        when(repo.findById(1L)).thenReturn(Optional.of(dbDeleted));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.getOr404(1L));
        assertStatus(ex, HttpStatus.NOT_FOUND);

        verify(cache, never()).putById(any());
    }

    // ------------------------
    // listActive()
    // ------------------------

    @Test
    void listActive_returnsCachedCopy_whenCacheHit() {
        List<Student> cached = List.of(student(1L, "A", 20, "a@u.es", false));
        when(cache.getAllActiveCached()).thenReturn(cached);

        List<Student> result = service.listActive();

        assertEquals(1, result.size());
        verify(repo, never()).findByDeletedFalse();
        verify(cache, never()).putAllActive(any());
    }

    @Test
    void listActive_queriesRepo_andCaches_whenCacheMiss() {
        when(cache.getAllActiveCached()).thenReturn(null);
        List<Student> fromRepo = List.of(student(1L, "A", 20, "a@u.es", false));
        when(repo.findByDeletedFalse()).thenReturn(fromRepo);

        List<Student> result = service.listActive();

        assertEquals(1, result.size());
        verify(cache).putAllActive(fromRepo);
    }

    // ------------------------
    // replace()
    // ------------------------

    @Test
    void replace_whenCorreoChangesAndExists_throws409() {
        Student existing = student(1L, "Ana", 20, "old@uni.es", false);
        when(cache.getById(1L)).thenReturn(existing);

        Student incoming = student(null, "Ana", 21, "new@uni.es", null);
        when(repo.existsByCorreoIgnoreCase("new@uni.es")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service.replace(1L, incoming));
        assertStatus(ex, HttpStatus.CONFLICT);

        verify(repo, never()).save(any());
    }

    @Test
    void replace_updatesFields_saves_andUpdatesCache() {
        Student existing = student(1L, "Ana", 20, "old@uni.es", false);
        when(cache.getById(1L)).thenReturn(existing);

        Student incoming = student(null, "Ana Mod", 21, "new@uni.es", null);
        when(repo.existsByCorreoIgnoreCase("new@uni.es")).thenReturn(false);

        Student saved = student(1L, "Ana Mod", 21, "new@uni.es", false);
        when(repo.save(any(Student.class))).thenReturn(saved);

        Student result = service.replace(1L, incoming);

        assertEquals("Ana Mod", result.getName());
        verify(cache).putById(saved);
        verify(cache).evictAllLists();
    }

    // ------------------------
    // patch()
    // ------------------------

    @Test
    void patch_whenEntityDeleted_andNotRestoreOnly_throws404() {
        Student existingDeleted = student(5L, "Ana", 20, "a@uni.es", true);
        when(repo.findById(5L)).thenReturn(Optional.of(existingDeleted));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.patch(5L, Map.of("age", 22)));
        assertStatus(ex, HttpStatus.NOT_FOUND);

        verify(repo, never()).save(any());
    }

    @Test
    void patch_restoreOnly_allowsRestoringDeletedEntity() {
        Student existingDeleted = student(5L, "Ana", 20, "a@uni.es", true);
        when(repo.findById(5L)).thenReturn(Optional.of(existingDeleted));

        // aquí no debería validar (restauración deja deleted=false y retorna tras save)
        Student saved = student(5L, "Ana", 20, "a@uni.es", false);
        when(repo.save(any(Student.class))).thenReturn(saved);

        Student result = service.patch(5L, Map.of("deleted", false));

        assertFalse(Boolean.TRUE.equals(result.getDeleted()));
        verify(cache).putById(saved);
        verify(cache).evictAllLists();
        verify(validator, never()).validate(any(Student.class));
    }

    @Test
    void patch_whenSetDeletedTrue_onlySavesAndEvictsCacheById() {
        Student existing = student(7L, "Ana", 20, "a@uni.es", false);
        when(repo.findById(7L)).thenReturn(Optional.of(existing));

        Student savedDeleted = student(7L, "Ana", 20, "a@uni.es", true);
        when(repo.save(any(Student.class))).thenReturn(savedDeleted);

        Student result = service.patch(7L, Map.of("deleted", true));

        assertTrue(Boolean.TRUE.equals(result.getDeleted()));
        verify(cache).evictById(7L);
        verify(cache).evictAllLists();
        verify(validator, never()).validate(any(Student.class));
    }

    @Test
    void patch_ageInvalid_throws400() {
        Student existing = student(9L, "Ana", 20, "a@uni.es", false);
        when(repo.findById(9L)).thenReturn(Optional.of(existing));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.patch(9L, Map.of("age", "noNumero")));
        assertStatus(ex, HttpStatus.BAD_REQUEST);

        verify(repo, never()).save(any());
    }

    @Test
    void patch_correoConflict_throws409() {
        Student existing = student(9L, "Ana", 20, "old@uni.es", false);
        when(repo.findById(9L)).thenReturn(Optional.of(existing));
        when(repo.existsByCorreoIgnoreCase("new@uni.es")).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.patch(9L, Map.of("correo", "new@uni.es")));
        assertStatus(ex, HttpStatus.CONFLICT);

        verify(repo, never()).save(any());
    }

    @Test
    void patch_validationFails_throws400_withMessages() {
        Student existing = student(11L, "Ana", 20, "a@uni.es", false);
        when(repo.findById(11L)).thenReturn(Optional.of(existing));

        @SuppressWarnings("unchecked")
        ConstraintViolation<Student> v = mock(ConstraintViolation.class);
        when(v.getMessage()).thenReturn("name no puede estar vacío");

        when(validator.validate(any(Student.class))).thenReturn(Set.of(v));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.patch(11L, Map.of("name", "")));
        assertStatus(ex, HttpStatus.BAD_REQUEST);
        assertNotNull(ex.getReason());
        assertTrue(ex.getReason().contains("name no puede estar vacío"));

        verify(repo, never()).save(any());
    }

    @Test
    void patch_updatesActiveFields_saves_andCaches() {
        Student existing = student(12L, "Ana", 20, "old@uni.es", false);
        when(repo.findById(12L)).thenReturn(Optional.of(existing));
        when(repo.existsByCorreoIgnoreCase("new@uni.es")).thenReturn(false);

        // Este caso sí llega a validate => stub aquí
        when(validator.validate(any(Student.class))).thenReturn(Set.of());

        ArgumentCaptor<Student> captor = ArgumentCaptor.forClass(Student.class);
        Student saved = student(12L, "Ana Mod", 22, "new@uni.es", false);
        when(repo.save(any(Student.class))).thenReturn(saved);

        Student result = service.patch(12L, Map.of(
                "name", "Ana Mod",
                "age", 22,
                "correo", "new@uni.es"
        ));

        verify(repo).save(captor.capture());
        Student toSave = captor.getValue();

        assertEquals("Ana Mod", toSave.getName());
        assertEquals(22, toSave.getAge());
        assertEquals("new@uni.es", toSave.getCorreo());

        assertEquals("Ana Mod", result.getName());
        verify(cache).putById(saved);
        verify(cache).evictAllLists();
    }

    // ------------------------
    // delete()
    // ------------------------

    @Test
    void delete_soft_setsDeletedTrue_andEvictsCache() {
        Student existing = student(20L, "Ana", 20, "a@uni.es", false);
        when(repo.findById(20L)).thenReturn(Optional.of(existing));

        service.delete(20L, true);

        assertTrue(Boolean.TRUE.equals(existing.getDeleted()));
        verify(repo).save(existing);
        verify(cache).evictById(20L);
        verify(cache).evictAllLists();
    }

    @Test
    void delete_hard_deletesById_andEvictsCache() {
        Student existing = student(21L, "Ana", 20, "a@uni.es", false);
        when(repo.findById(21L)).thenReturn(Optional.of(existing));

        service.delete(21L, false);

        verify(repo).deleteById(21L);
        verify(cache).evictById(21L);
        verify(cache).evictAllLists();
    }
}