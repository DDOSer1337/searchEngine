package searchengine.Busines.LinkHandling.Creator;

import searchengine.Busines.LinkHandling.DBConnector;
import searchengine.model.Index;
import searchengine.model.Lemma;
import searchengine.model.Page;
import searchengine.model.Repository.IndexRepository;

import java.sql.SQLException;
import java.sql.Statement;

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
        if (lemma != null && page != null) {
            Index index = new Index(page, lemma);
            if (isIndexExist(index)) {
                updateIndex(index);
            } else {
                try {
                    indexRepository.save(index);
                }
                catch (Exception e){
                    System.out.println("\nindex.getLemmaId() "+index.getLemmaId() +"\nindex.getPageId() "+ index.getPageId());
                }
            }
        }
    }

    private boolean isIndexExist(Index index) {
        String sql = "SELECT * FROM skillbox.indices WHERE lemmas_id = '" + index.getLemmaId() + "' " + "AND sites_id ='" + index.getPageId() + "'";
        try {
            DBConnector dbConnector = new DBConnector();
            Statement statement = dbConnector.getConnection().createStatement();
            boolean b = statement.executeQuery(sql).next();
            statement.close();
            return b;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void updateIndex(Index index) {
        String sql = "UPDATE `skillbox`.`indices`\n" +
                "SET\n" +
                "`rank` = rank+1 \n" +
                "WHERE `lemmas_id` = '" + index.getLemmaId() + "'" + "AND pages_id = '" + index.getPageId() + "'";
    }

}
