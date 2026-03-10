package com.example.StudentsApplication.repo;

import com.example.StudentsApplication.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*
*
* Clase para la llamada de algunos helpers útiles, en este caso para la busqueda
* y saber si mostrar o no estudiantes en caso de soft delete
*
*/

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByCorreoIgnoreCase(String correo);
    // Soft delete helpers:
    List<Student> findByDeletedFalse();               // listar solo no borrados
    boolean existsByIdAndDeletedFalse(Long id);       // existencia solo si no borrado
}

