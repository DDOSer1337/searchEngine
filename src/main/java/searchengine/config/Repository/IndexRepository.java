package searchengine.config.Repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.config.Index;

@Repository
public interface IndexRepository extends CrudRepository<Index,Integer> {
}
