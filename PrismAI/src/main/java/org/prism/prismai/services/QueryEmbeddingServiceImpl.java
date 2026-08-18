package org.prism.prismai.services;

import org.prism.prismai.entities.QueryEmbedding;
import org.prism.prismai.repository.QueryEmbeddingRepository;
import org.prism.prismai.services.interfaces.QueryEmbeddingService;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QueryEmbeddingServiceImpl implements QueryEmbeddingService {

  private final EmbeddingModel embeddingModel;
  private final QueryEmbeddingRepository repository;

  public QueryEmbeddingServiceImpl(EmbeddingModel embeddingModel, QueryEmbeddingRepository repository) {
    this.embeddingModel = embeddingModel;
    this.repository = repository;
  }

  @Override
  @Transactional
  public QueryEmbedding storeQuery(String query) {
    String normalized = query.trim();
    float[] embedding = embeddingModel.embed(normalized);
    return repository.save(new QueryEmbedding(normalized, embedding));
  }

  @Override
  @Transactional(readOnly = true)
  public List<QueryEmbedding> findSimilarQueries(String query, int limit) {
    return repository.findSimilar(embeddingModel.embed(query.trim()), limit);
  }
}
