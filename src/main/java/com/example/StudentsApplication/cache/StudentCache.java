package com.example.StudentsApplication.cache;

import com.example.StudentsApplication.model.Student;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Capa de caché explícita basada en Hazelcast IMap.
 * No introduce DTOs ni mappers; guarda Student tal cual.
 */
@Component
public class StudentCache {

    private static final String MAP_STUDENTS = "students";          // por id
    private static final String MAP_STUDENTS_LIST = "students_list"; // listas simples (opcional)

    private final IMap<Long, Student> byId;
    private final IMap<String, Collection<Student>> lists;

    public StudentCache(HazelcastInstance hz) {
        this.byId = hz.getMap(MAP_STUDENTS);
        this.lists = hz.getMap(MAP_STUDENTS_LIST);
    }

    // --------- Por ID ---------
    public Student getById(Long id) { return byId.get(id); }
    public void putById(Student s) {
        if (s != null && s.getId() != null) byId.put(s.getId(), s);
    }
    public void evictById(Long id) { byId.remove(id); }

    // --------- Listados (clave simple 'all') ---------
    public Collection<Student> getAllActiveCached() { return lists.get("all"); }
    public void putAllActive(Collection<Student> students) { lists.put("all", students); }
    public void evictAllLists() { lists.clear(); }

    // --------- Global ---------
    public void clearAll() {
        byId.clear();
        lists.clear();
    }

    // Test
    public boolean containsId(Long id) {
        return byId.containsKey(id);
    }
    public int sizeById() {
        return byId.size();
    }
    public int sizeLists() {
        return lists.size();
    }
    public boolean isListAllPresent() {
        return lists.containsKey("all");
    }
}