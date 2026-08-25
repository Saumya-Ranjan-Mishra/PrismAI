package org.prism.prismai.service;

import org.prism.prismai.AI_models.EmbeddingModelFactory;
import org.prism.prismai.DTO.QueryEmbeddingDto;
import org.prism.prismai.entities.QueryEmbedding;
import org.prism.prismai.entities.QueryMetadata;
import org.prism.prismai.repository.QueryMetadataRepository;
import org.prism.prismai.repository.QueryEmbeddingRepository;
import org.prism.prismai.service.interfaces.QueryEmbeddingService;
import org.prism.prismai.state.ChatServiceState;
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
  private final QueryMetadataRepository queryMetadataRepository;
  private static final String modelName = "miniLm";

  public QueryEmbeddingServiceImpl(EmbeddingModelFactory embeddingModel, QueryEmbeddingRepository repository,
      QueryMetadataRepository queryMetadataRepository) {
    this.embeddingModelFactory = embeddingModel;
    this.repository = repository;
    this.queryMetadataRepository = queryMetadataRepository;
  }

  @Override
  @Transactional
  public void storeQuery(String query) {
    TransformersEmbeddingModel embeddingModel = embeddingModelFactory.getModel(modelName);
    String normalized = query.trim();
    float[] embedding = embeddingModel.embed(normalized);

    repository.insertIfAbsentAndReturnId(normalized, QueryEmbeddingRepository.toVectorLiteral(embedding));
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

  @Override
  @Transactional
  public Long storeQueryAndMetadata(String query, ChatServiceState state) {
    TransformersEmbeddingModel embeddingModel = embeddingModelFactory.getModel(modelName);
    String normalized = query.trim();
    float[] embedding = embeddingModel.embed(normalized);

    Long queryEmbeddingId = repository.insertIfAbsentAndReturnId(
        normalized,
        QueryEmbeddingRepository.toVectorLiteral(embedding));

    QueryEmbedding queryEmbedding = repository.getReferenceById(queryEmbeddingId);
    QueryMetadata metadata = queryMetadataRepository.findByQueryEmbedding_Id(queryEmbeddingId)
        .orElseGet(QueryMetadata::new);

    metadata.setUserQueryIntent(state.getIntent());
    metadata.setServedFrom(state.getResponseProvider() == null ? null : state.getResponseProvider().toString());
    metadata.setTokenCount(state.getTokenCount());
    metadata.setQueryEmbedding(queryEmbedding);

    queryMetadataRepository.save(metadata);
    return queryEmbeddingId;
  }
}
