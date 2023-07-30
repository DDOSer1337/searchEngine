package searchengine.Busines.LinkHandling.Creator;

import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.Busines.LinkHandling.DBConnector;
import searchengine.Busines.LinkHandling.LinkParser;
import searchengine.model.Lemma;
import searchengine.model.Repository.LemmaRepository;
import searchengine.model.Site;

import java.io.IOException;
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
        LuceneMorphology luceneMorphology = getLuceneMorphology(word2LowerCase);
        if (luceneMorphology != null) {
            List<String> getMorphInfo = luceneMorphology.getMorphInfo(word2LowerCase);
            String morphInfo = getMorphInfo.get(0);
            if (isNotPartOfSpeech(morphInfo)) {
                List<String> word = luceneMorphology.getNormalForms(word2LowerCase);
                lemma = setLemma(word.get(0));
                if (isLemmaInDB(lemma)) {
                    updateLemma(lemma);
                } else {
                    lemmaRepository.save(lemma);
                }
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

    private LuceneMorphology getLuceneMorphology(String word2LowerCase) throws IOException {
        return isRussian(word2LowerCase) ? new RussianLuceneMorphology()
                : isEnglish(word2LowerCase) ? new EnglishLuceneMorphology() : null;
    }
    private boolean isRussian(String word) {
        return (word.matches("[а-яА-Я]+"));
    }

    private boolean isEnglish(String word) {
        return (word.matches("[a-zA-Z]+"));
    }

    private boolean isNotPartOfSpeech(String word) {
        return !(word.endsWith("Н") || word.endsWith("МЕЖД") || word.endsWith("СОЮЗ") ||
                word.endsWith("PREP") || word.endsWith("ADJECTIVE") || word.endsWith("CONJ") ||
                word.endsWith("ARTICLE") || word.endsWith("ADVERB"));
    }

    @SneakyThrows
    private boolean isLemmaInDB(Lemma lemma) {
        String sql = "SELECT * FROM skillbox.lemmas WHERE `lemma` = '" + lemma.getLemma() + "'" + "AND `sites_id` = '" + lemma.getSiteId().getId() + "'";
        return new DBConnector().getStatement().executeQuery(sql).next();
    }

    @SneakyThrows
    private void updateLemma(Lemma lemma) {
        String sql = "UPDATE `skillbox`.`lemmas`\n" +
                "SET\n" +
                "`frequency` = `frequency`+1\n" +
                "WHERE `lemma` = '" + lemma.getLemma() + "' " + "AND sites_id ='" + lemma.getSiteId().getId() + "'";
        new DBConnector().getStatement().executeUpdate(sql);
    }
}
