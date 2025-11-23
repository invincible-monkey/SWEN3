package at.technikum_wien.swen3.paperless.repository;

import at.technikum_wien.swen3.paperless.search.DocumentSearchEntity;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElasticSearchRepository extends ElasticsearchRepository<DocumentSearchEntity, Long> {

    // We use a "multi_match" query which is designed for full-text search across multiple fields.
    // "fuzziness": "AUTO" enables fuzzy matching (handling typos).
    // ?0 is the placeholder for the method parameter (the search string).
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title\", \"content\", \"summary\", \"tags\"], \"fuzziness\": \"AUTO\"}}")
    List<DocumentSearchEntity> searchByQuery(String query);
}