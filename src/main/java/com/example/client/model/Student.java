package com.example.client.model;

public class Student {
    private Long id;
    private String name;
    private Integer age;
    private String correo;
    private Boolean deleted;  // nuevo campo

    public Student() {}
    public Student(String name, Integer age, String correo) {
        this.name = name; this.age = age; this.correo = correo;
    }

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
