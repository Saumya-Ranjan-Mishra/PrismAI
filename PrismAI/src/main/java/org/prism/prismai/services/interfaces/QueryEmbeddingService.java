package org.prism.prismai.services.interfaces;

import org.prism.prismai.entities.QueryEmbedding;

import java.util.List;

public interface QueryEmbeddingService {
  QueryEmbedding storeQuery(String query);
  List<QueryEmbedding> findSimilarQueries(String query, int limit);
}
