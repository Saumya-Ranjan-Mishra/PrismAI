package org.prism.prismai.service.interfaces;

import org.prism.prismai.DTO.QueryEmbeddingDto;
import org.prism.prismai.entities.QueryEmbedding;

import java.util.List;

public interface QueryEmbeddingService {

  void storeQuery(String query);

  List<QueryEmbeddingDto> findSimilarQueries(String query, int limit);
}
