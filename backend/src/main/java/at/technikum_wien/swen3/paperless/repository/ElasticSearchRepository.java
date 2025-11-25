package at.technikum_wien.swen3.paperless.repository;

import at.technikum_wien.swen3.paperless.search.DocumentSearchEntity;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ElasticSearchRepository extends ElasticsearchRepository<DocumentSearchEntity, Long> {

    @Query("""
        {
          "multi_match": {
            "query": "?0",
            "fields": ["title", "content", "summary", "tags"],
            "fuzziness": "AUTO"
          }
        }
        """)
    List<DocumentSearchEntity> searchByQuery(String query);
}