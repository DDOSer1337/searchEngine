package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "indices")
public class Index{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "pages_id")
    private Page pageId;

    @ManyToOne
    @JoinColumn(name = "lemmas_id")
    private Lemma lemmaId;

    @Column(name = "`rank`",nullable = false)
    private float rank;

    public Index() {
    }

    public Index(Page pageId, Lemma lemmaId) {
        this.pageId = pageId;
        this.lemmaId = lemmaId;
        rank = 1;
    }


}
