package com.example.StudentsApplication.statistics.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.StudentsApplication.model.Student;
import com.example.StudentsApplication.repo.StudentRepository;
import com.example.StudentsApplication.statistics.client.StatisticsClient;
import com.example.StudentsApplication.statistics.dto.StatisticsDTO;
import com.example.StudentsApplication.protobuf.StatisticsResponse;


@Service
public class StatisticsService {

    private final StudentRepository studentRepository;
    private final StatisticsClient statisticsClient;

    public StatisticsService(StudentRepository studentRepository,
                             StatisticsClient statisticsClient) {
        this.studentRepository = studentRepository;
        this.statisticsClient = statisticsClient;
    }

    public StatisticsDTO calculateStatistics() {

        List<Student> students = studentRepository.findAll();

        StatisticsResponse response =
                statisticsClient.sendStudents(students);

        return new StatisticsDTO(
                response.getTotalStudents(),
                response.getAverageAge(),
                response.getMinAge(),
                response.getMaxAge(),
                response.getAdminCount(),
                response.getUserCount()
        );
    }
}