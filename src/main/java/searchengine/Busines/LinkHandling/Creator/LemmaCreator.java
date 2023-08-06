package searchengine.Busines.LinkHandling.Creator;

import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import searchengine.Busines.LinkHandling.DBConnector;
import searchengine.Busines.LinkHandling.LinkParser;
import searchengine.Busines.LinkHandling.Lucene;
import searchengine.model.Lemma;
import searchengine.model.Repository.LemmaRepository;
import searchengine.model.Site;

import java.io.IOException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class LemmaCreator {
    private final Site site;
    private final String text;
    private final LemmaRepository lemmaRepository;
    private volatile List<String> textList = new ArrayList<>();
    private volatile List<Lemma> lemmaList = new ArrayList<>();

    public LemmaCreator(Site site, String text, LemmaRepository lemmaRepository) {
        this.site = site;
        this.text = text;
        this.lemmaRepository = lemmaRepository;

    }

    public List<Lemma> getLemmas() {
        Collections.addAll(textList, text.split(" "));
        parseLemma();
        return lemmaList;
    }

    public void parseLemma() {
        for (String word : textList) {
            String word2LowerCase = word.toLowerCase(Locale.ROOT).trim();
            try {
                Lemma lemma = createLemma(word2LowerCase);
                if (lemma != null && !LinkParser.isNeedToStopTheParser()) {
                    lemmaList.add(lemma);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Lemma createLemma(String word2LowerCase) throws IOException {
        Lemma lemma = null;
        Lucene lucene = new Lucene(word2LowerCase);
        LuceneMorphology luceneMorphology = lucene.getLuceneMorphology();
        if (luceneMorphology != null && lucene.isNotPartOfSpeech()) {
            List<String> word = luceneMorphology.getNormalForms(word2LowerCase);
            lemma = setLemma(word.get(0));
            if (isLemmaInDB(lemma)) {
                updateLemma(lemma);
            } else {
                lemmaRepository.save(lemma);
            }
        }
        return lemma;
    }

    private Lemma setLemma(String word) {
        Lemma lemma = new Lemma();
        lemma.setSiteId(site);
        lemma.setLemma(word);
        lemma.setFrequency(1);
        return lemma;
    }

    @SneakyThrows
    private boolean isLemmaInDB(Lemma lemma) {
        String sql = "SELECT * FROM skillbox.lemmas WHERE `lemma` = '" + lemma.getLemma() + "'" + "AND `sites_id` = '" + lemma.getSiteId().getId() + "'";
        DBConnector dbConnector = new DBConnector();
        Statement statement = dbConnector.getConnection().createStatement();
        boolean b = statement.executeQuery(sql).next();
        statement.close();
        return b ;
    }

    @SneakyThrows
    private void updateLemma(Lemma lemma) {
        String sql = "UPDATE `skillbox`.`lemmas`\n" +
                "SET\n" +
                "`frequency` = `frequency`+1\n" +
                "WHERE `lemma` = '" + lemma.getLemma() + "' " + "AND sites_id ='" + lemma.getSiteId().getId() + "'";
        DBConnector dbConnector = new DBConnector();
        Statement statement = dbConnector.getConnection().createStatement();
        statement.executeUpdate(sql);
    }
}
