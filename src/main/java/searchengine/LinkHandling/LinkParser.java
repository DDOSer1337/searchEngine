package searchengine.LinkHandling;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.config.Repository.LemmaRepository;
import searchengine.config.Repository.PageRepository;
import searchengine.config.Repository.SiteRepository;
import searchengine.config.Site;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LinkParser {
    private final String url, startUrl;
    Set<String> verifiedLinks = Collections.synchronizedSet(new HashSet<>());

    @Autowired
    private SiteRepository siteRepository;
    @Autowired
    private LemmaRepository lemmaRepository;
    @Autowired
    private PageRepository pageRepository;


    public LinkParser(String url, String startUrl, String siteName) {
        this.url = url;
        this.startUrl = startUrl;
        parse(siteName);
    }

    private void parse(String siteName) {
        Site site = new Site("",startUrl,siteName);
        LinkCrawler linkCrawler = new LinkCrawler(url, startUrl, verifiedLinks,site);
    }

}
