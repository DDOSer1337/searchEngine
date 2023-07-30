package searchengine.Busines.LinkHandling.Creator;

import lombok.SneakyThrows;
import searchengine.Busines.LinkHandling.DBConnector;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Repository.IndexRepository;

import java.sql.ResultSet;
import java.sql.SQLException;

public class IndexCreator {
    private final Page page;
    private final Lemma lemma;
    private final IndexRepository indexRepository;

    public IndexCreator(Page page, Lemma lemma, IndexRepository indexRepository) {
        this.page = page;
        this.lemma = lemma;
        this.indexRepository = indexRepository;
    }

    public void indexCreate() {
        if (lemma != null && page!=null) {
            Index index = new Index(page, lemma);
            if (isIndexExist(index)) {
                updateIndex(index);
            } else {
                indexRepository.save(index);
            }
        }
    }

    private boolean isIndexExist(Index index) {
        String sql = "SELECT * FROM skillbox.indices WHERE lemmas_id = '" + getLemmaId() + "' " + "AND sites_id ='" + getPageId() + "'";
        try {
            return new DBConnector().getStatement().executeQuery(sql).next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @SneakyThrows
    private int getPageId() {
        int i = -1;
        String sql = "SELECT * FROM skillbox.pages WHERE path = '" + page.getPath() + "'" + "AND sites_id ='" + page.getSiteId() + "'";
        ResultSet resultSet = new DBConnector().getStatement().executeQuery(sql);
        if (resultSet.next()) {
            i = resultSet.getInt("id");
        }
        return i;
    }

    @SneakyThrows
    private int getLemmaId() {
        int i = -1;
        String sql = "SELECT * FROM skillbox.lemmas WHERE `lemma` = '" + lemma.getLemma() + "'" + "AND `sites_id` = '" + lemma.getSiteId().getId() + "'";
        ResultSet resultSet = new DBConnector().getStatement().executeQuery(sql);
        if (resultSet.next()) {
            i = resultSet.getInt("id");
        }
        return i;
    }
    private void updateIndex(Index index){
        String sql = "UPDATE `skillbox`.`indices`\n" +
                "SET\n" +
                "`rank` = rank+1 \n" +
                "WHERE `lemmas_id` = '" + index.getLemmaId() + "'" + "AND pages_id = '" + index.getPageId() +"'";
    }

}
