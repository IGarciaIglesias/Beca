package com.example.studentsapplication.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.studentsapplication.statistics.dto.StatisticsDTO;
import com.example.studentsapplication.statistics.service.StatisticsService;

@RestController
@RequestMapping("/students/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping
    public StatisticsDTO getStatistics() {
        return statisticsService.calculateStatistics();
    }
}
