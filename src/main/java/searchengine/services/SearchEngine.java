package searchengine.services;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import org.springframework.stereotype.Service;
import searchengine.Busines.LinkHandling.Lucene;
import searchengine.Busines.LinkHandling.DBConnector;
import searchengine.dto.Search.WordRank;
import searchengine.dto.Search.SearchData;
import searchengine.dto.Search.TotalSearchData;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Site;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class SearchEngine {
    private final String siteURL;
    private final String query;
    private String[] words;
    private float maxAbsoluteRelevance = 0;

    public TotalSearchData getStatistics() {
        return getTotal();
    }

    private TotalSearchData getTotal() {
        TotalSearchData totalSearchData = new TotalSearchData();
        List<SearchData> searchData= new ArrayList<>();
        int siteCount = -1;
        if (siteURL == null) {
            siteCount = 3;
            for (int i = 1; i <= siteCount; i++) {
                searchData.add(getSearchData(i));
            }
        } else {
            getSearchData(siteCount);
        }
        for (SearchData data : searchData) {
            data.setRelativeRelevance(relativeRelevance(data));
        }
        totalSearchData.setSearchData(searchData);
        return totalSearchData;
    }

    private SearchData getSearchData(int siteNumber) {
        SearchData searchData = new SearchData();
        List<WordRank> wordRanks = new ArrayList<>();
        for (String w : words) {
            String normalWord = getNormalForm(w);
            if (isLemmaInDB(normalWord)) {
                List<Index> list = getIndex();
                if (list != null) {
                    WordRank wordRank = new WordRank();
                    wordRank.setWord(normalWord);
                    wordRank.setWordRank(getRank(list));
                    wordRanks.add(wordRank);
                }
            }
        }
        searchData.setPageNumber(siteNumber);
        searchData.setWordRanks(wordRanks);
        searchData.setAbsoluteRelevance(absoluteRelevance(wordRanks));
        return searchData;
    }

    private float getRank(List<Index> list) {
        float allLemmaRank = 0;
        for (Index i : list) {
            allLemmaRank += i.getRank();
        }
        return allLemmaRank / list.size();
    }

    private List<Index> getIndex() {
        List<Site> sitesId = getSiteId();
        List<Index> indexesId = new ArrayList<>();
        if (sitesId != null) {
            for (Site siteId : sitesId) {
                List<Page> page = getPageId(siteId);
                List<Lemma> lemma = getLemmaId(siteId);
                if (page != null && lemma != null) {
                    for (Page p : page) {
                        for (Lemma l : lemma) {
                            try {
                                Index index = new Index();
                                String sql = "SELECT * FROM skillbox.indices WHERE pages_id = '" + p.getId() + "' AND lemmas_id = '" + l.getLemma() + "'";
                                DBConnector dbConnector = new DBConnector();
                                Statement statement = dbConnector.getConnection().createStatement();
                                ResultSet resultSet = statement.executeQuery(sql);
                                while (resultSet.next()) {
                                    index.setRank(resultSet.getInt("rank"));
                                    indexesId.add(index);
                                }
                                statement.close();
                                return indexesId;
                            } catch (SQLException e) {
                                System.out.println("Ошибка: " + "page: " + p.getId() + " Lemma: " + l.getId());
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private List<Page> getPageId(Site siteId) {
        String sql = "SELECT * FROM skillbox.pages WHERE sites_id = '" + siteId + "'";
        List<Page> pages = new ArrayList<>();
        try {
            Page page = new Page();
            DBConnector dbConnector = new DBConnector();
            Statement statement = dbConnector.getConnection().createStatement();
            ResultSet resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                page.setId(resultSet.getInt("id"));
                pages.add(page);
            }
            statement.close();
            return pages;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Lemma> getLemmaId(Site siteId) {
        List<Lemma> lemmas = new ArrayList<>();
        for (String word : words) {
            String wordNormalForm = getNormalForm(word);
            String sql = "SELECT * FROM skillbox.lemmas WHERE sites_id = '" + siteId + "' AND lemmas_id = '" + wordNormalForm + "'";
            try {
                DBConnector dbConnector = new DBConnector();
                Statement statement = dbConnector.getConnection().createStatement();
                ResultSet resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    Lemma lemma = new Lemma();
                    lemma.setId(resultSet.getInt("id"));
                    lemmas.add(lemma);
                }
                statement.close();
                return lemmas;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private List<Site> getSiteId() {
        List<Site> sites = new ArrayList<>();
        Site site = new Site();
        try {
            DBConnector dbConnector = new DBConnector();
            Statement statement = dbConnector.getConnection().createStatement();
            if (siteURL != null) {
                String[] getDomain = siteURL.split("/");
                String domain = getDomain[2];
                String sql = "SELECT * FROM skillbox.sites WHERE name = '" + domain + "'";
                ResultSet resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    site.setId(resultSet.getInt("id"));
                    sites.add(site);
                }
                statement.close();
                return sites;
            } else {
                String sql = "SELECT * FROM skillbox.sites";

                ResultSet resultSet = statement.executeQuery(sql);
                while (resultSet.next()) {
                    site.setId(resultSet.getInt("id"));
                    sites.add(site);
                }
                statement.close();
                return sites;
            }
        } catch (SQLException e) {
            return null;
        }
    }

    private float absoluteRelevance(List<WordRank> wordRanks) {
        float rank = 0;
        for (WordRank wordRank: wordRanks){
            rank += wordRank.getWordRank();
        }
        rank = rank/wordRanks.size();
        if (maxAbsoluteRelevance <= rank){
            maxAbsoluteRelevance=rank;
        }
        return rank;
    }

    private float relativeRelevance(SearchData searchData) {
        return searchData.getAbsoluteRelevance()/maxAbsoluteRelevance;
    }

    @SneakyThrows
    private String getNormalForm(String word) {
        Lucene lucene = new Lucene(word);
        LuceneMorphology luceneMorphology = lucene.getLuceneMorphology();
        if (luceneMorphology != null) {
            if (lucene.isNotPartOfSpeech()) {
                return luceneMorphology.getNormalForms(word).get(0);
            }
        }
        return null;
    }

    @SneakyThrows
    private boolean isLemmaInDB(String lemma) {
        String sql = "SELECT * FROM skillbox.lemmas WHERE `lemma` = '" + lemma + "'" + "AND `sites_id` = '" + getSiteId() + "'";
        DBConnector dbConnector = new DBConnector();
        Statement statement = dbConnector.getConnection().createStatement();
        boolean b = statement.executeQuery(sql).next();
        statement.close();
        return b;
    }
}