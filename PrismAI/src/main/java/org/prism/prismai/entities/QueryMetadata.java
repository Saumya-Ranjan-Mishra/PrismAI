package org.prism.prismai.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class QueryMetadata {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String userQueryIntent;
  private String servedFrom;
  private long tokenCount;

  @OneToOne
  @JoinColumn(name = "query_embedding_id", unique = true, nullable = false)
  private QueryEmbedding queryEmbedding;

  public QueryMetadata(String userQueryIntent, String servedFrom, long tokenCount, QueryEmbedding queryEmbedding) {
    this.userQueryIntent = userQueryIntent;
    this.tokenCount = tokenCount;
    this.servedFrom = servedFrom;
    this.queryEmbedding = queryEmbedding;
  }
}
