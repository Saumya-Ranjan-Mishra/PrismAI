package org.prism.prismai.service;

import org.prism.prismai.AI_models.EmbeddingModelFactory;
import org.prism.prismai.DTO.QueryEmbeddingDto;
import org.prism.prismai.repository.QueryEmbeddingRepository;
import org.prism.prismai.service.interfaces.QueryEmbeddingService;
import org.springframework.ai.transformers.TransformersEmbeddingModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
  public void storeQuery(String query) {
    TransformersEmbeddingModel embeddingModel = embeddingModelFactory.getModel(modelName);
    String normalized = query.trim();
    float[] embedding = embeddingModel.embed(normalized);

    repository.insertIfAbsent(normalized, QueryEmbeddingRepository.toVectorLiteral(embedding));
  }

  @Override
  @Transactional(readOnly = true)
  public List<QueryEmbeddingDto> findSimilarQueries(String query, int limit) {
    TransformersEmbeddingModel embeddingModel = embeddingModelFactory.getModel(modelName);
    Optional<List<QueryEmbeddingDto>> result = repository.findSimilar(embeddingModel.embed(query.trim()), limit);

    if (result.isPresent()) {
      return result.get();
    }

    return new ArrayList<QueryEmbeddingDto>();
  }
}
