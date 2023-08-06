package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import searchengine.Busines.LinkHandling.DBConnector;
import searchengine.Busines.LinkHandling.LinkParser;
import searchengine.model.Repository.IndexRepository;
import searchengine.model.Repository.LemmaRepository;
import searchengine.model.Repository.PageRepository;
import searchengine.model.Repository.SiteRepository;
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

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SitesList sites;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    String[] statuses = {"INDEXED", "FAILED", "INDEXING"};
    String[] errors = {
            "Ошибка индексации: главная страница сайта не доступна",
            "Ошибка индексации: сайт не доступен",
            ""
    };
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
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        for (Site site : sites.getSites()) {
            DetailedStatisticsItem item = getDetailedStatisticsItem(statuses, errors, site);
            total.setPages(total.getPages() + getPageCount(site));
            total.setLemmas(total.getLemmas() + getLemmaCount(site));
            detailed.add(item);
        }
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
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
            item.setStatus(getStatus(site));
            item.setError(getError(site));

        } catch (SQLException e) {
            e.printStackTrace();
            item.setLemmas(0);
            item.setPages(0);
            item.setError(errors[2]);
            item.setStatus(statuses[1]);
        }
        item.setStatusTime(System.currentTimeMillis());
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
    private String getStatus(Site site) throws SQLException {
        String i = null;
        String queryForGetPageCount = "SELECT * FROM skillbox.sites AS s WHERE s.site_status = '" + site.getName() + "'";
        ResultSet pageCount = statement.executeQuery(queryForGetPageCount);
        if (pageCount.next()) {
            i = pageCount.getString("site_status");
        }
        return i;
    }
    private String getError(Site site) throws SQLException {
        String i = null;
        String queryForGetPageCount = "SELECT * FROM skillbox.sites AS s WHERE s.last_error = '" + site.getName() + "'";
        ResultSet pageCount = statement.executeQuery(queryForGetPageCount);
        if (pageCount.next()) {
            i = pageCount.getString("last_error");
        }
        return i;
    }

    public void startParse() throws SQLException {
        for (Site site : sites.getSites()) {
            LinkParser linkParser = new LinkParser(site.getUrl(), siteRepository, pageRepository, lemmaRepository, indexRepository);
            linkParser.parse();
        }
    }
}

