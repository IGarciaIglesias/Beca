package com.example.StudentsApplication.statistics.dto;

public record StatisticsDTO(
        int totalStudents,
        double averageAge,
        int minAge,
        int maxAge,
        int adminCount,
        int userCount
) {}
