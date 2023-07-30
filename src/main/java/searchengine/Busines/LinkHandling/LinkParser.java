package searchengine.Busines.LinkHandling;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import searchengine.dto.statistics.ExceptionData;
import searchengine.dto.statistics.Result;
import searchengine.model.Repository.IndexRepository;
import searchengine.model.Repository.LemmaRepository;
import searchengine.model.Repository.PageRepository;
import searchengine.model.Repository.SiteRepository;
import searchengine.model.Site;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
public class LinkParser {
    private String domain;
    private final String startUrl;
    private Set<String> verifiedLinks = Collections.synchronizedSet(new HashSet<>());
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    public static boolean needToStopTheParser;


    public void parse() {
        needToStopTheParser = false;
        String[] getDomain = startUrl.split("/");
        domain = getDomain[2];
        if (checkUrl()) {
            Site site = new Site(startUrl, domain);
            if (checkAvailabilityInDB()) {
                String sql = "DELETE FROM skillbox.sites WHERE name = '" + domain + "'";
                try {
                    getStatement().executeUpdate(sql);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            siteRepository.save(site);
            LinkCrawler linkCrawler = new LinkCrawler(domain, startUrl, verifiedLinks, site, siteRepository, pageRepository, lemmaRepository, indexRepository);
            linkCrawler.compute();
        }
    }

    public boolean checkUrl() {
        return startUrl != null && (startUrl.matches("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"));
    }

    @SneakyThrows
    public boolean checkAvailabilityInDB() {
        String sql = "SELECT * FROM skillbox.sites WHERE name = '" + domain + "'";
        return getStatement().executeQuery(sql).next();
    }

    private Statement getStatement() throws SQLException {
        return new DBConnector().getConnection().createStatement();
    }

    public static ResponseEntity<?> stopParse(){
        Result result = new Result();
        if (isNeedToStopTheParser()){
            ExceptionData exceptionData = new ExceptionData();
            result.setResult(false);
            exceptionData.setError("Индексация не запущена");
            exceptionData.setResult(result);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionData);
        }else {
            needToStopTheParser = true;
            result.setResult(true);
            return ResponseEntity.status(HttpStatus.OK).body(result);
        }
    }
    public static boolean isNeedToStopTheParser() {
        return needToStopTheParser;
    }
}
