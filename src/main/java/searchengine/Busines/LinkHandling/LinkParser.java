package searchengine.Busines.LinkHandling;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import searchengine.model.Repository.IndexRepository;
import searchengine.model.Repository.LemmaRepository;
import searchengine.model.Repository.PageRepository;
import searchengine.model.Repository.SiteRepository;
import searchengine.model.Site;

import java.sql.ResultSet;
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


    public void parse() {

        String[] getDomain = startUrl.split("/");
        domain = getDomain[2];
        if (checkUrl() && !checkAvailabilityInDB()) {
            Site site = new Site(startUrl, domain);
            if (!checkAvailabilityInDB()) {
                siteRepository.save(site);
            }
            else {
                //написать удаление и создание
            }
            LinkCrawler linkCrawler = new LinkCrawler(domain, startUrl, verifiedLinks, site, siteRepository, pageRepository, lemmaRepository, indexRepository);
            linkCrawler.compute();
        }
    }

    public boolean checkUrl() {
        return startUrl != null && (startUrl.matches("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"));
    }

    @SneakyThrows
    public boolean checkAvailabilityInDB() {
        boolean b = false;
        DBConnector dbConnector = new DBConnector();
        Statement statement = dbConnector.getConnection().createStatement();
        String sql = "SELECT * FROM skillbox.sites WHERE name = '" + domain + "'";
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            b = true;
        }
        return b;
    }
}
