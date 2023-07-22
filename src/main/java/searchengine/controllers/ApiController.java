package searchengine.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsServiceImpl;

import java.sql.SQLException;


@RestController
@RequestMapping("/api")
public class ApiController {

    private StatisticsServiceImpl statisticsService;

    public ApiController(StatisticsServiceImpl statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
            return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<StatisticsResponse> startIndexing() {
        try {
            statisticsService.startParse();
        } catch (SQLException e) {
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getStatistics());
    }
}
