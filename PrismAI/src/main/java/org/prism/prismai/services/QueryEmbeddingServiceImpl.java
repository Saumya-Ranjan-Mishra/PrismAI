package org.prism.prismai.services;

import org.prism.prismai.AI_models.EmbeddingModelFactory;
import org.prism.prismai.entities.QueryEmbedding;
import org.prism.prismai.repository.QueryEmbeddingRepository;
import org.prism.prismai.services.interfaces.QueryEmbeddingService;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QueryEmbeddingServiceImpl implements QueryEmbeddingService {

  private final EmbeddingModelFactory embeddingModelFactory;
  private final QueryEmbeddingRepository repository;
  private static final String modelName = "miniLm";

  public QueryEmbeddingServiceImpl(EmbeddingModelFactory embeddingModel, QueryEmbeddingRepository repository) {
    this.embeddingModelFactory = embeddingModel;
    this.repository = repository;
  }

  @Override
  @Transactional
  public QueryEmbedding storeQuery(String query) {
    TransformersEmbeddingModel embeddingModel = embeddingModelFactory.getModel(modelName);
    String normalized = query.trim();
    float[] embedding = embeddingModel.embed(normalized);
    return repository.save(new QueryEmbedding(normalized, embedding));
  }

  @Override
  @Transactional(readOnly = true)
  public List<QueryEmbedding> findSimilarQueries(String query, int limit) {
    TransformersEmbeddingModel embeddingModel = embeddingModelFactory.getModel(modelName);
    return repository.findSimilar(embeddingModel.embed(query.trim()), limit);
  }
}
