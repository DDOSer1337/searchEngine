package searchengine.LinkHandling;

import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.config.Index;
import searchengine.config.Lemma;
import searchengine.config.Page;
import searchengine.config.Repository.IndexRepository;
import searchengine.config.Repository.LemmaRepository;
import searchengine.config.Repository.PageRepository;
import searchengine.config.Repository.SiteRepository;
import searchengine.config.Site;

import java.util.List;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

public class LinkCrawler extends RecursiveAction {

    private final String domain, currentLink;
    private final Set<String> verifiedLinks;
    private final Site site;

    @Autowired
    private PageRepository pageRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private IndexRepository indexRepository;


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
            Page page = new Page(newLink,connection.get(),domain,site,connection.execute().statusCode());
            pageRepository.save(page);
            LemmaSearch lemmaSearch = new LemmaSearch(site, connection.get().text());

            List<Lemma> lemmaList = lemmaSearch.invoke();
            for (Lemma lemma: lemmaList){
                Index index = new Index(page,lemma);
                indexRepository.save(index);
            }

            LinkCrawler recursiveTask = new LinkCrawler(newLink, domain, verifiedLinks, site);
            recursiveTask.fork();
        }
    }

    private boolean isCorrectUrl(String newLink) {
        return newLink.contains("http")
                && !newLink.contains("pdf")
                && !newLink.contains("?")
                && newLink.contains(domain)
                && !newLink.contains("#");
    }


}
