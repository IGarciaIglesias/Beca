package com.example.StudentsApplication;

import com.example.StudentsApplication.model.Student;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/*
*
* Inicializa datos y endpoints falsos para el testeo, es el primer boceto de JUnit que hice
*
*/

public class FakeStudentRepository {

    private final Map<Long, Student> data = new HashMap<>();
    private final AtomicLong seq = new AtomicLong(0);

    public Student save(Student s) {
        if (s.getId() == null) {
            s.setId(seq.incrementAndGet());
        }
        data.put(s.getId(), s);
        return s;
    }

    public Optional<Student> findById(Long id) {
        return Optional.ofNullable(data.get(id));
    }

    public List<Student> findAll() {
        return new ArrayList<>(data.values());
    }

    public void deleteById(Long id) {
        data.remove(id);
    }
}