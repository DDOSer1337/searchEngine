package searchengine.LinkHandling;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import searchengine.config.Enum.siteStatus;
import searchengine.config.Repository.PageRepository;
import searchengine.config.Repository.SiteRepository;
import searchengine.config.Site;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public class Test {
    private PageRepository pageRepository;
    private SiteRepository siteRepository;
    public static void main(String[] args) throws IOException {
        LuceneMorphology luceneMorphology = new RussianLuceneMorphology();
        List<String> test = luceneMorphology.getMorphInfo("собачка");

        Site site = new Site();
        site.setSiteStatus(siteStatus.INDEXING);
        site.setUrl("https://www.lenta.ru");
        site.setName("lenta.ru");
        site.setLastError("");
        site.setStatusTime(LocalDateTime.now());

        StandardServiceRegistry standardServiceRegistry = new StandardServiceRegistryBuilder().build();
        Metadata metadata = new MetadataSources(standardServiceRegistry).getMetadataBuilder().build();
        SessionFactory sessionFactory = metadata.getSessionFactoryBuilder().build();
        Session session = sessionFactory.openSession();

        String hql = "From " + Site.class.getSimpleName();
        List<Site> list = session.createQuery(hql).getResultList();

        System.out.println(list.size());

    }
}
