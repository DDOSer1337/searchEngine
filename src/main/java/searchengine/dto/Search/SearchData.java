package searchengine.dto.Search;

import lombok.Data;

import java.util.List;

@Data
public class SearchData {
    int pageNumber;
    List<WordRank> wordRanks;
    float absoluteRelevance;
    float relativeRelevance;
}
