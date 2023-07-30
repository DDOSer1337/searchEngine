package searchengine.Busines.LinkHandling;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import searchengine.Busines.LinkHandling.Creator.IndexCreator;
import searchengine.Busines.LinkHandling.Creator.LemmaCreator;
import searchengine.Busines.LinkHandling.Creator.PageCreator;
import searchengine.model.Enum.siteStatus;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Repository.IndexRepository;
import searchengine.model.Repository.LemmaRepository;
import searchengine.model.Repository.PageRepository;
import searchengine.model.Repository.SiteRepository;
import searchengine.model.Site;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

@RequiredArgsConstructor
public class LinkCrawler extends RecursiveAction {

    private final String domain, currentLink;
    private final Set<String> verifiedLinks;
    private final Site site;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    @SneakyThrows
    @Override
    protected void compute() {
        Thread.sleep(500);
        if (!(verifiedLinks.contains(currentLink) && LinkParser.isNeedToStopTheParser())) {
            linkChecking();
        }
    }

    private void linkChecking() {
        Connection connection = Jsoup.connect(currentLink)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT " +
                        "5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com");
        try {
            List<Element> links = connection.get().select("a[href]");
            Document document = connection.get();
            System.out.println("\nподключено :" + currentLink + "\n");
            for (Element link : links) {
                String newLink = link.attr("abs:href");
                recursiveActionFork(newLink, document);
            }
            verifiedLinks.add(currentLink);
        } catch (IOException e) {
            exceptionSite(site);
            e.printStackTrace();
        }
    }

    private void recursiveActionFork(String newLink, Document document) {
        if (isCorrectUrl(newLink) && !verifiedLinks.contains(newLink)) {
            PageCreator pageCreator = new PageCreator(newLink,siteRepository,lemmaRepository,pageRepository);
            Page page = pageCreator.getPage();
            List<Lemma> lemmaList =  new LemmaCreator(site, document.text(), lemmaRepository).getLemmas();
            for (Lemma lemma : lemmaList){
                IndexCreator indexCreator = new IndexCreator(page,lemma,indexRepository);
                indexCreator.indexCreate();
            }
            LinkCrawler recursiveTask = new LinkCrawler(newLink, domain, verifiedLinks, site, siteRepository, pageRepository, lemmaRepository, indexRepository);
            recursiveTask.fork();
        }
    }

    private boolean isCorrectUrl(String newLink) {
        return newLink != null
                && newLink.contains("http")
                && !newLink.contains("pdf")
                && !newLink.contains("?")
                && newLink.contains(domain)
                && !newLink.contains("#");
    }
    private void exceptionSite(Site site){
        String sql = "UPDATE `skillbox`.`sites`\n" +
                "SET\n" +
                "`site_status` = '"+siteStatus.FAILED+"',\n" +
                "`last_error` = 'Ошибка индексации: главная страница сайта не доступна'\n" +
                "WHERE `url` = '" + site.getUrl()+"'";
        try {
            new DBConnector().getStatement().executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
