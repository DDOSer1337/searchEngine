package searchengine.Busines.LinkHandling.Creator;

import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import searchengine.Busines.LinkHandling.DBConnector;
import searchengine.dto.statistics.ExceptionData;
import searchengine.dto.statistics.Result;
import searchengine.model.Page;
import searchengine.model.Repository.LemmaRepository;
import searchengine.model.Repository.PageRepository;
import searchengine.model.Repository.SiteRepository;
import searchengine.model.Site;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PageCreator {
    private Page page = null;
    private String domain;
    private String link;
    private SiteRepository siteRepository;
    private LemmaRepository lemmaRepository;
    private PageRepository pageRepository;

    public PageCreator(String link, SiteRepository siteRepository, LemmaRepository lemmaRepository, PageRepository pageRepository) {
        this.domain = getDomain(link);
        this.link = link;
        this.siteRepository = siteRepository;
        this.lemmaRepository = lemmaRepository;
        this.pageRepository = pageRepository;
        createOrUpdatePage();
    }

    private void createOrUpdatePage() {
        Connection connection = getConnection(link);
        try {
            Site site = getOrCreateSite(siteRepository);
            Document document = connection.get();
            page = new Page(link, document, domain, site, connection.execute().statusCode());
            if (isPageExist(page)) {
                deletePage();
            } else {
                pageRepository.save(page);
            }
            new LemmaCreator(site, document.text(), lemmaRepository).parseLemma();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Connection getConnection(String link) {
        return Jsoup.connect(link)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT " +
                        "5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com");
    }

    private String getDomain(String link) {
        String[] getDomain = link.split("/");
        return getDomain[2];
    }

    @SneakyThrows
    private void deletePage() {
        String sql = "DELETE FROM skillbox.pages WHERE path = '" + page.getPath() + "'" + "AND sites_id ='" + page.getSiteId() + "'";
        new DBConnector().getStatement().executeUpdate(sql);
    }


    private static boolean isPageExist(Page page) {
        String sql = "SELECT * FROM skillbox.pages WHERE path = '" + page.getPath() + "'" + "AND sites_id ='" + page.getSiteId() + "'";
        try {
            return new DBConnector().getStatement().executeQuery(sql).next();
        } catch (SQLException e) {
            return false;
        }
    }

    @SneakyThrows
    private Site getOrCreateSite(SiteRepository siteRepository) {
        Site site;
        String sql = "SELECT * FROM skillbox.sites WHERE name = '" + domain + "'";
        ResultSet resultSet = new DBConnector().getStatement().executeQuery(sql);
        if (resultSet.next()) {
            site = new Site();
            site.setId(resultSet.getInt("id"));
            site.setName(resultSet.getString("name"));
        } else {
            site = new Site(link, domain);
            siteRepository.save(site);
        }
        return site;
    }

    public Page getPage() {
        return page;
    }

    public static ResponseEntity<?> getPageResponse(Page page) {
        Result result = new Result();
        if (isPageExist(page)) {
            result.setResult(true);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        } else {
            ExceptionData exceptionData = new ExceptionData();
            result.setResult(false);
            exceptionData.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
            exceptionData.setResult(result);
            return ResponseEntity.status(HttpStatus.OK).body(exceptionData);
        }
    }
}
