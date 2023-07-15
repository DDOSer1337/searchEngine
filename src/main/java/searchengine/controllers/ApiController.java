package searchengine.controllers;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;


@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;

    public ApiController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        if (statisticsService.getStatistics().isResult()) {
            return ResponseEntity.ok(statisticsService.getStatistics());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    @GetMapping("/startIndexing/")
    public ResponseEntity<?> startIndexing() {
        Connection document = Jsoup.connect("https://www.lenta.ru")
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT " +
                        "5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com");
        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getStatistics());
    }
}
