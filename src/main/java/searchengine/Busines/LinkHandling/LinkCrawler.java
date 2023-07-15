package searchengine.Busines.LinkHandling;

import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

public class LinkCrawler extends RecursiveAction {

    private final String domain, currentLink;
    private final Set<String> verifiedLinks;
    private final Site site;
    private final DBConnector dbConnector = new DBConnector();


    public LinkCrawler(String domain, String currentLink, Set<String> verifiedLinks, Site site) {
        this.domain = domain;
        this.currentLink = currentLink;
        this.verifiedLinks = verifiedLinks;
        this.site = site;

    }

    @SneakyThrows
    @Override
    protected void compute() {
        Thread.sleep(200);
        if (!verifiedLinks.contains(currentLink)) {
            linkChecking();
        }
    }

    @SneakyThrows
    private void linkChecking() {
        Connection connection = Jsoup.connect(currentLink)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT " +
                        "5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com");
        for (Element link : connection.get().select("a[href]")) {
            String newLink = link.attr("abs:href");
            recursiveActionFork(newLink, connection);
        }
        verifiedLinks.add(currentLink);
    }

    @SneakyThrows
    private void recursiveActionFork(String newLink, Connection connection) {
        if (isCorrectUrl(newLink)) {
            Page page = new Page(newLink, connection.get(), domain, site, connection.execute().statusCode());
            pageUploader(page);
            LemmaSearch lemmaSearch = new LemmaSearch(site, connection.get().text());
            List<Lemma> lemmaList = lemmaSearch.invoke();
            IndexCreator(page, lemmaList);
            LinkCrawler recursiveTask = new LinkCrawler(newLink, domain, verifiedLinks, site);
            recursiveTask.fork();
        }
    }

    private void pageUploader(Page page) throws SQLException {
        dbConnector.getConnection().createStatement().executeQuery("INSERT INTO `skillbox`.`pages`\n" +
                "(`id`,\n" +
                "`code`,\n" +
                "`content`,\n" +
                "`path`,\n" +
                "`sites_id`)\n" +
                "VALUES\n" +
                "("+ page.getId()+",\n" +
                page.getCode()+",\n" +
                page.getContent()+",\n" +
                page.getPath()+",\n" +
                page.getSiteId()+");");
        System.out.println("pageUploader");
    }
    private void IndexCreator(Page page, List<Lemma> lemmaList) throws SQLException {
        int counterForUploadingToDatabase = 0;
        StringBuilder builder = new StringBuilder();
        for (Lemma lemma : lemmaList) {
            Index index = new Index(page, lemma);
            String indexData = " (" + index.getId() + ", " + index.getRank() + index.getLemmaId() + ", " + index.getPageId() + ")";
            builder.append(builder.length() == 0 ? "" : ",").append(indexData);
            counterForUploadingToDatabase++;
            if (counterForUploadingToDatabase == 50){
                indexUploader(builder.toString());
                builder = new StringBuilder();
            }
        }
        indexUploader(builder.toString());

    }

    private boolean isCorrectUrl(String newLink) {
        return newLink.contains("http")
                && !newLink.contains("pdf")
                && !newLink.contains("?")
                && newLink.contains(domain)
                && !newLink.contains("#");
    }
    private void indexUploader(String content) throws SQLException {
        String sql = "INSERT INTO `skillbox`.`indices`\n" +
                "(`id`,\n" +
                "`rank`,\n" +
                "`lemmas_id`,\n" +
                "`pages_id`) VALUES" + content + "ON DUPLICATE KEY UPDATE `rank` = `rank` + 1 ";
        dbConnector.getConnection().createStatement().executeQuery(sql);
    }
}
