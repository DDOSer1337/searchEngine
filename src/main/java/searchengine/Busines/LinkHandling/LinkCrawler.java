package searchengine.Busines.LinkHandling;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Repository.IndexRepository;
import searchengine.model.Repository.LemmaRepository;
import searchengine.model.Repository.PageRepository;
import searchengine.model.Repository.SiteRepository;
import searchengine.model.Site;
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
    private DBConnector dbConnector = new DBConnector();

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
            pageRepository.save(page);
            LemmaSearch lemmaSearch = new LemmaSearch(site, connection.get().text());
            List<Lemma> lemmaList = lemmaSearch.getLemmas();
            IndexCreator(page, lemmaList);
            LinkCrawler recursiveTask = new LinkCrawler(newLink, domain, verifiedLinks, site, siteRepository, pageRepository, lemmaRepository, indexRepository);
            recursiveTask.fork();
        }
    }

    private void IndexCreator(Page page, List<Lemma> lemmaList){
        for (Lemma lemma : lemmaList) {
            Index index = new Index(page, lemma);
            indexRepository.save(index);
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
