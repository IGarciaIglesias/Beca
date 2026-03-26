package com.example.studentsapplication.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Profesores")
public class Profesor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String correo;

    // ===== getters =====

    public Long getId() {
        return id;
    }

    public String getCorreo() {
        return correo;
    }

    // ===== setters =====

    public void setId(Long id) {
        this.id = id;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }
}