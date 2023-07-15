package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import searchengine.Busines.LinkHandling.DBConnector;
import searchengine.Busines.LinkHandling.LinkParser;
import searchengine.model.Site;
import searchengine.model.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final SitesList sites;
    DBConnector dbConnector = new DBConnector();
    Statement statement;

    {
        try {
            statement = dbConnector.getConnection().createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = {"INDEXED", "FAILED", "INDEXING"};
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();

        for (Site site : sitesList) {
            LinkParser linkParser = new LinkParser(site.getUrl());
            DetailedStatisticsItem item = getDetailedStatisticsItem(statuses, errors, site);
            total.setPages(getPageCount(site));
            total.setLemmas(getLemmaCount(site));
            detailed.add(item);
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        // не трогать
        response.setResult(true);
        return response;
    }

    @SneakyThrows
    private DetailedStatisticsItem getDetailedStatisticsItem(String[] statuses, String[] errors, Site site) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(site.getName());
        item.setUrl(site.getUrl());

        try {
            item.setPages(getPageCount(site));
            item.setLemmas(getLemmaCount(site));
            item.setStatus(statuses[2]);
            item.setError(errors[2]);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            item.setLemmas(0);
            item.setPages(0);
            item.setError(errors[2]);
            item.setStatus(statuses[1]);
        }
        item.setStatusTime(System.currentTimeMillis() - (random.nextInt(10_000)));
        return item;
    }

    private int getLemmaCount(Site site) throws SQLException {
        int i = 0;
        String queryForGetLemmaCount = "SELECT Count(*) FROM skillbox.lemmas AS l JOIN skillbox.sites AS s ON l.sites_id = s.id WHERE s.name = '" + site.getName() + "'";
        ResultSet lemmaCount = statement.executeQuery(queryForGetLemmaCount);
        if (lemmaCount.next()) {
            i = lemmaCount.getInt("Count(*)");
        }
        return i;
    }

    private int getPageCount(Site site) throws SQLException {
        int i = 0;
        String queryForGetPageCount = "SELECT Count(*) FROM skillbox.pages AS p JOIN skillbox.sites AS s ON p.sites_id = s.id WHERE s.name = '" + site.getName() + "'";
        ResultSet pageCount = statement.executeQuery(queryForGetPageCount);
        if (pageCount.next()) {
            i = pageCount.getInt("Count(*)");
        }
        return i;
    }
}

