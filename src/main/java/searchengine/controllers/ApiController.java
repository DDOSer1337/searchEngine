package searchengine.controllers;

import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.config.Enum.siteStatus;
import searchengine.config.Page;
import searchengine.config.Repository.PageRepository;
import searchengine.config.Repository.SiteRepository;
import searchengine.config.Site;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.StatisticsService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticsService statisticsService;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private PageRepository pageRepository;

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

    @SneakyThrows
    @GetMapping("/startIndexing/")
    public ResponseEntity<StatisticsResponse> startIndexing() {
        Site site = new Site();
        site.setSiteStatus(siteStatus.INDEXING);
        site.setUrl("https://www.lenta.ru");
        site.setName("lenta.ru");
        site.setLastError("");
        site.setStatusTime(LocalDateTime.now());

        siteRepository.save(site);

        Connection document = Jsoup.connect("https://www.lenta.ru")
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT " +
                        "5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com");

        Page page = new Page(site.getUrl(), document.get(), site.getUrl(), site, document.execute().statusCode());
        pageRepository.save(page);

        return ResponseEntity.status(HttpStatus.OK).body(new StatisticsResponse());
    }
}
