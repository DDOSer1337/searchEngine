package searchengine.Busines;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.Busines.LinkHandling.DBConnector;
import searchengine.model.Enum.siteStatus;
import searchengine.model.Site;


import java.io.IOException;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class Test {
    public static void main(String[] args) throws IOException, SQLException {
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        List<String> test = luceneMorphology.getMorphInfo("собачка");

        Site site = new Site();
        site.setSiteStatus(siteStatus.INDEXING);
        site.setUrl("https://lenta.ru/");
        site.setName("lenta.ru");
        site.setLastError("");
        site.setStatusTime(LocalDateTime.now());
        test();
    }

    private static void test() throws SQLException {
        DBConnector dbConnector = new DBConnector();
        String sql = "SELECT Count(*) FROM skillbox.pages AS p JOIN skillbox.sites AS s ON p.sites_id = s.id WHERE s.name = 'lenta.ru'";
        Statement statement = dbConnector.getConnection().createStatement();
        ResultSet q = statement.executeQuery(sql);

        while (q.next()) {
            System.out.println(q.getInt("Count(*)"));
        }
        q.close();
        statement.close();
    }

    public static boolean checkUrl(String s) {
        return s != null && (s.matches("^(https?)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"));
    }
}
