package searchengine.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.Busines.LinkHandling.Creator.PageCreator;
import searchengine.Busines.LinkHandling.LinkParser;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.Page;
import searchengine.model.Repository.LemmaRepository;
import searchengine.model.Repository.PageRepository;
import searchengine.model.Repository.SiteRepository;
import searchengine.services.SearchEngine;
import searchengine.services.StatisticsServiceImpl;

import java.sql.SQLException;


@RestController
@RequestMapping("/api")
public class ApiController {
    private StatisticsServiceImpl statisticsService;
    private SearchEngine searchEngine;
    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private LemmaRepository lemmaRepository;

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

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(statisticsService.getStatistics());
        }
        return ResponseEntity.status(HttpStatus.OK).body(statisticsService.getStatistics());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<?> stopIndexing() {
        return LinkParser.stopParse();
    }

    @PostMapping("/indexPage")
    public ResponseEntity<?> indexPage(@RequestParam("url") String url) {
        Page page = new PageCreator(url, siteRepository, lemmaRepository, pageRepository).getPage();
        return PageCreator.getPageResponse(page);

    }
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam("query") String query,@RequestParam(value = "site", required = false) String site) {
        searchEngine = new SearchEngine(site,query);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
