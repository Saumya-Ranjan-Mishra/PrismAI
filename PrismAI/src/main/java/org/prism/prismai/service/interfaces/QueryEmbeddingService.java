package org.prism.prismai.service.interfaces;

import org.prism.prismai.DTO.QueryEmbeddingDto;
import org.prism.prismai.state.ChatServiceState;

import java.util.List;

public interface QueryEmbeddingService {
  void storeQuery(String query);

  List<QueryEmbeddingDto> findSimilarQueries(String query, int limit);

  void storeQueryAndMetadata(String query, ChatServiceState state);
}
