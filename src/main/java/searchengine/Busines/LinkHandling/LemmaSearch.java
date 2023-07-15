package searchengine.Busines.LinkHandling;

import searchengine.model.Lemma;
import searchengine.model.Site;

import java.util.List;

import java.util.concurrent.RecursiveTask;

public class LemmaSearch extends RecursiveTask<List<Lemma>> {
    private Site link;
    private String text;
    private String sql = "INSERT INTO `skillbox`.`lemmas`(`id`," +
            "`frequency`," +
            "`lemma`," +
            "`sites_id`) VALUES" +"content"+
            "ON DUPLICATE KEY UPDATE `frequency` = `frequency` + 1 ";

    public LemmaSearch(Site link, String text) {
        this.link = link;
        this.text = text;
    }


    @Override
    protected List<Lemma> compute() {
        return null;
    }
}
