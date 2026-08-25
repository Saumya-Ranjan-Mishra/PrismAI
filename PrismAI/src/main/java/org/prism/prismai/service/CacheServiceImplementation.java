package org.prism.prismai.service;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

import org.prism.prismai.entities.QueryEmbedding;
import org.prism.prismai.repository.DocumentStorageRepository;
import org.prism.prismai.repository.QueryEmbeddingRepository;
import org.prism.prismai.service.interfaces.CacheService;
import org.springframework.cache.Cache;

@Service
public class CacheServiceImplementation implements CacheService {
  private final CacheManager cacheManager;
  private final QueryEmbeddingRepository queryEmbeddingRepository;
  private final DocumentStorageRepository docStorageRepo;

  public CacheServiceImplementation(CacheManager cacheManager, QueryEmbeddingRepository queryEmbeddingRepository,
      DocumentStorageRepository docStorageRepo) {
    this.cacheManager = cacheManager;
    this.queryEmbeddingRepository = queryEmbeddingRepository;
    this.docStorageRepo = docStorageRepo;
  }

  public String getValue(String key) {
    Cache cache = cacheManager.getCache("llmResponses");
    String cachedResponse = cache.get(key, String.class);
    if (cachedResponse != null) {
      return cachedResponse;
    }

    Optional<QueryEmbedding> responseFromEmbeddingStore = queryEmbeddingRepository.findByQueryIgnoreCase(key);

    if (responseFromEmbeddingStore.isPresent()) {
      return docStorageRepo.getDocumentById(responseFromEmbeddingStore.get().getId().toString());
    }

    return null;
  }

  public void saveValue(String key, String value) {
    Cache cache = cacheManager.getCache("llmResponses");
    if (cache != null) {
      cache.put(key, value);
    }
  }
}
