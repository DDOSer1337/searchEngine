package searchengine.Busines.LinkHandling;

import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.Repository.SiteRepository;
import searchengine.model.Site;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LinkParser {
    private final String domain, startUrl;
    private final Set<String> verifiedLinks = Collections.synchronizedSet(new HashSet<>());
    private String siteException;

    @Autowired
    private SiteRepository siteRepository;

    public LinkParser(String startUrl) {
        String[] getDomain = startUrl.split("/");
        this.domain = getDomain[2] + "/";
        this.startUrl = startUrl;
        parse();
    }

    private void parse() {
        if (checkUrl(startUrl)) {
            String sql = "SELECT * FROM skillbox.sites  WHERE `name` = '" + domain + "'";
            try {
                DBConnector dbConnector = new DBConnector();
                Statement statement = dbConnector.getConnection().createStatement();
                ResultSet q = statement.executeQuery(sql);

                if (q.next()) {
                    statement.executeQuery("DELETE FROM skillbox.sites WHERE `name` = '" + domain + "'");
                }
                Site site = new Site(startUrl, domain);
                siteUploader(statement, site);
                LinkCrawler linkCrawler = new LinkCrawler(domain, startUrl, verifiedLinks, site);
            } catch (SQLException e) {
                siteException = e.getMessage();
            }
        }
    }

    private void siteUploader(Statement statement, Site site) throws SQLException {
        statement.executeQuery("INSERT INTO `skillbox`.`sites`\n" +
                "(`id`,\n" +
                "`last_error`,\n" +
                "`name`,\n" +
                "`site_status`,\n" +
                "`status_time`,\n" +
                "`url`)\n" +
                "VALUES\n" +
                "(" + site.getId() + ",\n" +
                site.getLastError() + ">,\n" +
                site.getName() + ",\n" +
                site.getSiteStatus() + ",\n" +
                site.getSiteStatus() + ",\n" +
                site.getUrl() + ");");
    }

    public static boolean checkUrl(String s) {
        return s != null && (s.matches("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"));
    }

    public String getSiteException() {
        return siteException;
    }
}
