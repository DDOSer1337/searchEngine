package searchengine.Busines.LinkHandling;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;

public class Lucene {
    private String word;

    public Lucene(String word) {
        this.word = word;
    }

    public LuceneMorphology getLuceneMorphology() throws IOException {
        return isRussian() ? new RussianLuceneMorphology()
                : isEnglish() ? new EnglishLuceneMorphology() : null;
    }

    boolean isRussian() {
        return (word.matches("[а-яА-Я]+"));
    }

    boolean isEnglish() {
        return (word.matches("[a-zA-Z]+"));
    }

    public boolean isNotPartOfSpeech() {
        return !(word.endsWith("Н") || word.endsWith("МЕЖД") || word.endsWith("СОЮЗ") ||
                word.endsWith("PREP") || word.endsWith("ADJECTIVE") || word.endsWith("CONJ") ||
                word.endsWith("ARTICLE") || word.endsWith("ADVERB"));
    }
}