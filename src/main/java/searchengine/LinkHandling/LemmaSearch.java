package searchengine.LinkHandling;

import searchengine.config.Lemma;
import searchengine.config.Site;

import java.util.List;

import java.util.concurrent.RecursiveTask;

public class LemmaSearch extends RecursiveTask<List<Lemma>> {
    private Site link;
    private String text;

    public LemmaSearch(Site link, String text) {
        this.link = link;
        this.text = text;
    }

    @Override
    protected List<Lemma> compute() {
        return null;
    }
}
