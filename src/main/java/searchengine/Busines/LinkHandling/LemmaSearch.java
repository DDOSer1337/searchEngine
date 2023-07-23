package searchengine.Busines.LinkHandling;

import lombok.SneakyThrows;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import searchengine.model.Lemma;
import searchengine.model.Site;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;


public class LemmaSearch {
    private final Site site;
    private final String text;

    public LemmaSearch(Site site, String text) {
        this.site = site;
        this.text = text;
    }

    @SneakyThrows
    public List<Lemma> getLemmas() {
        List<Lemma> lemmaList = new ArrayList<>();
        List<String> textList = new ArrayList<>();
        Collections.addAll(textList, text.split(" "));
        for (String word : textList) {
            String word2LowerCase = word.toLowerCase(Locale.ROOT).trim();
            Lemma lemma = createLemma(word2LowerCase);
            if (lemma != null) {
                lemmaList.add(lemma);
            }
        }
        return lemmaList;
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

    private Lemma createLemma(String word2LowerCase) throws IOException {
        Lemma lemma = null;
        LuceneMorphology luceneMorphology = null;
        if (isRussian(word2LowerCase)) {
            luceneMorphology = new RussianLuceneMorphology();
        }
        if (isEnglish(word2LowerCase)) {
            luceneMorphology = new EnglishLuceneMorphology();
        }
        if (luceneMorphology != null) {
            List<String> getMorphInfo = luceneMorphology.getMorphInfo(word2LowerCase);
            String morphInfo = getMorphInfo.get(0);
            if (isNotPartOfSpeech(morphInfo)) {
                List<String> we = luceneMorphology.getNormalForms(word2LowerCase);
                lemma = new Lemma();
                lemma.setSiteId(site);
                lemma.setLemma(we.get(0));
                lemma.setFrequency(1);
            }
        }
        return lemma;
    }
}
