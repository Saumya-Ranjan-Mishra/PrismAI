package org.prism.prismai.service.QueryResponseStrategyImplemetations;

import java.util.Optional;

import org.prism.prismai.entities.QueryEmbedding;
import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.repository.DocumentStorageRepository;
import org.prism.prismai.repository.QueryEmbeddingRepository;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;
import org.springframework.stereotype.Component;

@Component
public class L2CacheResponseStrategy implements QueryResponseStrategy {
  private final QueryEmbeddingRepository queryEmbeddingRepository;
  private final DocumentStorageRepository docStorageRepository;

  public L2CacheResponseStrategy(QueryEmbeddingRepository queryEmbeddingRepository,
      DocumentStorageRepository docStorageRepo) {
    this.queryEmbeddingRepository = queryEmbeddingRepository;
    this.docStorageRepository = docStorageRepo;
  }

  @Override
  public ResponseProviders provider() {
    return ResponseProviders.L2Cache;
  }

  @Override
  public String getResponse(String userQuery) {
    Optional<QueryEmbedding> responseFromEmbeddingStore = queryEmbeddingRepository.findByQueryIgnoreCase(userQuery);

    if (responseFromEmbeddingStore.isPresent()) {
      return docStorageRepository.getDocumentById(responseFromEmbeddingStore.get().getId().toString());
    }

    return null;
  }
}
