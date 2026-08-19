package org.prism.prismai.repository;

import org.prism.prismai.entities.QueryEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.StringJoiner;

@Repository
public interface QueryEmbeddingRepository extends JpaRepository<QueryEmbedding, Long> {

  @Query(value = """
      SELECT * FROM query_embedding
      ORDER BY embedding <=> CAST(:embedding AS vector)
      LIMIT :limit
      """, nativeQuery = true)
  List<QueryEmbedding> findNearest(@Param("embedding") String embedding, @Param("limit") int limit);

  default List<QueryEmbedding> findSimilar(float[] embedding, int limit) {
    return findNearest(toVectorLiteral(embedding), limit);
  }

  private static String toVectorLiteral(float[] embedding) {
    StringJoiner joiner = new StringJoiner(",", "[", "]");
    for (float value : embedding) {
      joiner.add(Float.toString(value));
    }
    return joiner.toString();
  }
}
