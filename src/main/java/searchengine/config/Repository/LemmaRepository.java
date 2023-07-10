package searchengine.config.Repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.config.Lemma;

@Repository
public interface LemmaRepository extends CrudRepository<Lemma,Integer> {
}
