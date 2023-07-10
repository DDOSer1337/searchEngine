package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import searchengine.config.Lemma;
import searchengine.config.Page;
import searchengine.config.Repository.LemmaRepository;
import searchengine.config.Repository.PageRepository;
import searchengine.config.Repository.SiteRepository;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final Random random = new Random();
    private final SitesList sites;

    @Autowired
    private final SiteRepository siteRepository;
    @Autowired
    private final PageRepository pageRepository;
    @Autowired
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        String[] statuses = {"INDEXED", "FAILED", "INDEXING"};
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };
        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.getSites().size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        List<Site> sitesList = sites.getSites();

        for (int i = 0; i < sitesList.size(); i++) {
            Site site = sitesList.get(i);
            int pages = getPages(site).size();
            int lemmas = getLemma(site).size();
            DetailedStatisticsItem item = getDetailedStatisticsItem(statuses, errors, i, site, pages, lemmas);
            total.setPages(total.getPages() + item.getPages());
            total.setLemmas(total.getLemmas() + item.getLemmas());
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        // не трогать
        response.setResult(true);
        return response;
    }

    private List<Lemma> getLemma(Site site) {
        List<Lemma> lemmaList = new ArrayList<>();
        Iterable<Lemma> lemmaIterable = lemmaRepository.findAll();
        for (Lemma lemma : lemmaIterable) {
            if (lemma.getSiteId().equals(site)) {
                lemmaList.add(lemma);
            }
        }
        return lemmaList;
    }

    private List<Page> getPages(Site site) {
        List<Page> pageList = new ArrayList<>();
        Iterable<Page> pageIterable = pageRepository.findAll();
        for (Page page : pageIterable) {
            if (page.getSiteId().equals(site)){
                pageList.add(page);
            }
        }
        return pageList;
    }

    private DetailedStatisticsItem getDetailedStatisticsItem(String[] statuses, String[] errors, int i, Site site, int pages, int lemmas) {
        DetailedStatisticsItem item = new DetailedStatisticsItem();
        item.setName(site.getName());
        item.setUrl(site.getUrl());
        item.setPages(pages);
        item.setLemmas(lemmas);
        item.setStatus(statuses[i % 3]);
        item.setError(errors[i % 3]);
        item.setStatusTime(System.currentTimeMillis() - (random.nextInt(10_000)));
        return item;
    }
}

