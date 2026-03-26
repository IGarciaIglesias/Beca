package com.example.studentsapplication.repo;

import com.example.studentsapplication.model.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
}