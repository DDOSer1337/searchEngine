package searchengine.config.Repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import searchengine.config.Site;

@Repository
public interface SiteRepository extends CrudRepository<Site,Integer> {
}
