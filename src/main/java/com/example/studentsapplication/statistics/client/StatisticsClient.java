package com.example.studentsapplication.statistics.client;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.studentsapplication.model.Student;
import com.example.studentsapplication.protobuf.StudentData;
import com.example.studentsapplication.protobuf.StatisticsRequest;
import com.example.studentsapplication.protobuf.StatisticsResponse;

@Component
public class StatisticsClient {

    public StatisticsResponse sendStudents(List<Student> students) {

        List<StudentData> protoStudents = students.stream()
                .map(s -> StudentData.newBuilder()
                        .setId(s.getId())
                        .setAge(s.getAge())
                        .setRole(s.getRole().name())
                        .build())
                .toList();

        StatisticsRequest request = StatisticsRequest.newBuilder()
                .addAllStudents(protoStudents)
                .build();

        // Simulación de API externa
        return calculateLocally(request);
    }

    /**
     * Simula el comportamiento de una Statistics API externa
     * (mañana esto puede ser REST Protobuf o gRPC)
     */
    private StatisticsResponse calculateLocally(StatisticsRequest request) {

        var stats = request.getStudentsList()
                .stream()
                .mapToInt(StudentData::getAge)
                .summaryStatistics();

        long admins = request.getStudentsList().stream()
                .filter(s -> s.getRole().equals("ADMIN"))
                .count();

        long users = request.getStudentsList().stream()
                .filter(s -> s.getRole().equals("USER"))
                .count();

        return StatisticsResponse.newBuilder()
                .setTotalStudents((int) stats.getCount())
                .setAverageAge(stats.getAverage())
                .setMinAge(stats.getMin())
                .setMaxAge(stats.getMax())
                .setAdminCount((int) admins)
                .setUserCount((int) users)
                .build();
    }
}
