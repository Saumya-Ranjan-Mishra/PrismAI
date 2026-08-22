package org.prism.prismai.repository;

import org.prism.prismai.DTO.QueryEmbeddingDto;
import org.prism.prismai.entities.QueryEmbedding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

@Repository
public interface QueryEmbeddingRepository extends JpaRepository<QueryEmbedding, Long> {

  @Modifying
  @Transactional
  @Query(value = """
      INSERT INTO query_embedding (query, embedding)
      VALUES (:query, CAST(:embedding AS vector))
      ON CONFLICT (query) DO NOTHING
      """, nativeQuery = true)
  int insertIfAbsent(@Param("query") String query, @Param("embedding") String embedding);

  @Query(value = """
      SELECT query,
             ROUND(
                 (1 - (embedding <=> CAST(:embedding AS vector)))::numeric,
                 2
             ) AS precisionScore
      FROM query_embedding
      ORDER BY embedding <=> CAST(:embedding AS vector)
      LIMIT :limit
      """, nativeQuery = true)
  Optional<List<QueryEmbeddingDto>> findNearest(@Param("embedding") String embedding,
      @Param("limit") int limit);

  default Optional<List<QueryEmbeddingDto>> findSimilar(float[] embedding, int limit) {
    return findNearest(toVectorLiteral(embedding), limit);
  }

  public static String toVectorLiteral(float[] embedding) {
    StringJoiner joiner = new StringJoiner(",", "[", "]");
    for (float value : embedding) {
      joiner.add(Float.toString(value));
    }
    return joiner.toString();
  }
}
