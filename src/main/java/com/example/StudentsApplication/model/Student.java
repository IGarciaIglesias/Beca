package com.example.StudentsApplication.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "students", uniqueConstraints = {
        @UniqueConstraint(name = "uk_students_correo", columnNames = "correo")
})
public class Student {

    public enum Role {
        ADMIN, USER
    }

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

    @NotNull(message = "El rol es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER; // ✅ default en Java (además del default en BDD)

    @Column(nullable = false)
    private Boolean deleted = false;

    public Student() {}

    public Student(String name, Integer age, String correo, Role role) {
        this.name = name;
        this.age = age;
        this.correo = correo;
        this.deleted = false;
        this.role = (role == null ? Role.USER : role);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public Role getRole() { return role; }                 // ✅ devuelve Role
    public void setRole(Role role) { this.role = role; }   // ✅ recibe Role

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }
}