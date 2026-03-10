package com.example.StudentsApplication;

import com.example.StudentsApplication.model.Student;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ConstraintViolation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/*
*
* Tests de los validadores de inserción de datos, comprueba que de error cuando tiene que darlo
*
*/

public class StudentValidationTest {

    private Validator validator;

    @BeforeEach
    void setup() {
        // Crea el validador de Jakarta
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void valida_student_correcto() {
        Student s = new Student("Ana", 20, "ana@uni.es");
        Set<ConstraintViolation<Student>> errores = validator.validate(s);

        assertTrue(errores.isEmpty(), "No debería haber errores");
    }

    @Test
    void valida_student_sin_name() {
        Student s = new Student("", 20, "ana@uni.es");
        Set<ConstraintViolation<Student>> errores = validator.validate(s);

        assertFalse(errores.isEmpty(), "Debe fallar porque name está vacío");
    }

    @Test
    void valida_student_email_invalido() {
        Student s = new Student("Ana", 20, "correo-malo");
        Set<ConstraintViolation<Student>> errores = validator.validate(s);

        assertFalse(errores.isEmpty(), "Debe fallar porque el email es inválido");
    }

    @Test
    void valida_student_age_negativa_o_cero() {
        Student s = new Student("Ana", 0, "ana@uni.es");
        Set<ConstraintViolation<Student>> errores = validator.validate(s);

        assertFalse(errores.isEmpty(), "Debe fallar porque la edad debe ser >= 1");
    }
}