package searchengine.model;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.jsoup.nodes.Document;

import javax.persistence.*;
import javax.persistence.Index;

@Getter
@Setter
@Entity
@Table(name = "pages",indexes = @Index(columnList = "path"))
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sites_id")
    private Site siteId;

    @Column(name = "path",nullable = false)
    private String path;

    @Column(name = "code",nullable = false)
    private Integer code;

    @Column(name = "content",nullable = false)
    @Type(type = "org.hibernate.type.TextType")
    private String content;

    public Page() {
    }

    public Page(String newLink, Document document,String domain,Site siteId, Integer code) {
        int start = newLink.indexOf(domain) + domain.length();
        int end = newLink.length();
        this.path = newLink.substring(start, end);
        this.content = document.toString();
        this.siteId = siteId;
        this.code = code;
    }
}
