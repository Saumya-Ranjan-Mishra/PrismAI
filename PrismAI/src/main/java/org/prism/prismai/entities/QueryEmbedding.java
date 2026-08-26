package org.prism.prismai.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "query_embedding")
@Getter
@Setter
@NoArgsConstructor
public class QueryEmbedding {

  public static final int EMBEDDING_DIMENSIONS = 384;

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "query", nullable = false, columnDefinition = "text")
  private String query;

  @JdbcTypeCode(SqlTypes.VECTOR)
  @Array(length = EMBEDDING_DIMENSIONS)
  @Column(name = "embedding", nullable = false)
  private float[] embedding;

  @Generated(event = EventType.INSERT)
  @Column(name = "created_at", insertable = false, updatable = false)
  private Instant createdAt;

  @OneToOne(mappedBy = "queryEmbedding")

  private QueryMetadata metadata;

  public QueryEmbedding(String query, float[] embedding) {
    this.query = query;
    this.embedding = embedding;
  }
}
