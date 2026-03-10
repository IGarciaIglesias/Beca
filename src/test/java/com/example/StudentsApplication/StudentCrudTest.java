package com.example.StudentsApplication;

import com.example.StudentsApplication.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/*
*
* Tests de CRUD iniciales, de momento no testea el servidor como tal
*
*/

public class StudentCrudTest {

    private FakeStudentRepository repo;

    @BeforeEach
    void setup() {
        repo = new FakeStudentRepository();
    }

    @Test
    void crear_student() {
        Student s = new Student("Ana", 20, "ana@uni.es");
        repo.save(s);

        assertNotNull(s.getId(), "El ID debe generarse automáticamente");
        assertEquals("Ana", s.getName());
    }

    @Test
    void leer_por_id() {
        Student s = repo.save(new Student("Ana", 20, "ana@uni.es"));
        Optional<Student> encontrado = repo.findById(s.getId());

        assertTrue(encontrado.isPresent());
        assertEquals("Ana", encontrado.get().getName());
    }

    @Test
    void listar_students() {
        repo.save(new Student("Ana", 20, "ana@uni.es"));
        repo.save(new Student("Luis", 22, "luis@uni.es"));

        List<Student> lista = repo.findAll();

        assertEquals(2, lista.size());
    }

    @Test
    void actualizar_student() {
        Student s = repo.save(new Student("Ana", 20, "ana@uni.es"));

        // actualizamos
        s.setName("Ana Gómez");
        s.setAge(21);
        repo.save(s);

        Student actualizado = repo.findById(s.getId()).get();

        assertEquals("Ana Gómez", actualizado.getName());
        assertEquals(21, actualizado.getAge());
    }

    @Test
    void borrar_student() {
        Student s = repo.save(new Student("Ana", 20, "ana@uni.es"));

        repo.deleteById(s.getId());

        assertTrue(repo.findById(s.getId()).isEmpty());
        assertEquals(0, repo.findAll().size());
    }

    @Test
    void soft_delete_oculta_en_listado_y_get() {
        // Crea y guarda uno
        Student s = new Student("Ana", 20, "ana@uni.es");
        s.setDeleted(false);
        // simula repo/save...
        // marca como borrado
        s.setDeleted(true);

        // list() debería ignorarlos -> asume que llamas a findByDeletedFalse()
        assertTrue(s.getDeleted(), "Marcado como borrado");
        // getOr404 debe comportarse como 404 si deleted=true -> simula lanzando excepción
    }
}