package com.example.StudentsApplication.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/*
*
* Clase modelo de datos de estudiantes, gestiona getters y setters de los datos de los estudiantes
* así como los mensajes de las comprobaciones de inserción del usuario
*
*/

@Entity
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(name = "uk_students_correo", columnNames = "correo")
})
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "La edad es obligatoria")
    @Min(value = 1, message = "La edad debe ser >= 1")
    @Column(nullable = false)
    private Integer age;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    @Column(nullable = false)
    private String correo;

    @Column(nullable = false)
    private Boolean deleted = false;

    public Student() {

    }
    public Student(String name, Integer age, String correo) {
        this.name = name;
        this.age = age;
        this.correo = correo;
        this.deleted = false;
    }
    // Getters y Setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Integer getAge() {
        return age;
    }
    public void setAge(Integer age) {
        this.age = age;
    }
    public String getCorreo() {
        return correo;
    }
    public void setCorreo(String correo) {
        this.correo = correo;
    }
    public Boolean getDeleted() {
        return deleted;
    }
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }
}
