package searchengine.config.Repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.config.Page;

@Repository
public interface PageRepository extends CrudRepository<Page,Integer> {
}
