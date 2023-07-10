package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import searchengine.config.Enum.siteStatus;
import searchengine.config.Repository.SiteRepository;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Setter
@Getter
@Entity
@Table(name = "sites")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "site_status", columnDefinition = "enum ('INDEXING','INDEXED','FAILED')")
    private searchengine.config.Enum.siteStatus siteStatus;

    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(nullable = false, length = 255)
    private String url;

    @Column(nullable = false, length = 255)
    private String name;

    public Site() {
    }

    public Site(String lastError, String url, String name) {

        this.siteStatus = searchengine.config.Enum.siteStatus.INDEXING;
        this.statusTime = LocalDateTime.now();
        this.lastError = lastError;
        this.url = url;
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Site site = (Site) o;
        return Objects.equals(url, site.url) && Objects.equals(name, site.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name);
    }
}
