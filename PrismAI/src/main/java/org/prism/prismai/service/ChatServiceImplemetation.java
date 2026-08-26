package org.prism.prismai.service;

import java.util.List;

import org.prism.prismai.DTO.ChatResponseDto;
import org.prism.prismai.DTO.QueryEmbeddingDto;
import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.repository.DocumentStorageRepository;
import org.prism.prismai.service.interfaces.CacheService;
import org.prism.prismai.service.interfaces.ChatService;
import org.prism.prismai.service.interfaces.IntentClassificationService;
import org.prism.prismai.service.interfaces.QueryEmbeddingService;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;
import org.prism.prismai.service.interfaces.QueryResponseStrategyFactory;
import org.prism.prismai.state.ChatServiceState;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImplemetation implements ChatService {

  private final CacheService cacheService;
  private final QueryResponseStrategyFactory queryResponseStrategyFactory;
  private final QueryEmbeddingService queryEmbeddingService;
  private final IntentClassificationService intentClassificationService;
  private final DocumentStorageRepository docStorageRepository;

  public ChatServiceImplemetation(CacheService cacheService,
      QueryResponseStrategyFactory queryResponseStrategyFactory, QueryEmbeddingService queryEmbeddingService,
      IntentClassificationService intentClassificationService, DocumentStorageRepository documentStorageRepository) {
    this.queryEmbeddingService = queryEmbeddingService;
    this.cacheService = cacheService;
    this.queryResponseStrategyFactory = queryResponseStrategyFactory;
    this.intentClassificationService = intentClassificationService;
    this.docStorageRepository = documentStorageRepository;
  }

  @Override
  public ChatResponseDto serveUserQuery(String userQuery) {
    ChatServiceState state = new ChatServiceState();

    String responseFromCache = cacheService.getValue(userQuery);

    if (responseFromCache != null) {
      return new ChatResponseDto("L1Cache", responseFromCache, "NA", "NA", 0.0);
    }

    List<QueryEmbeddingDto> dto = queryEmbeddingService.findSimilarQueries(userQuery, 1);

    if (!dto.isEmpty()) {
      state.setPrecisionScore(dto.get(0).getPrecisionScore().doubleValue());
    }

    if (state.getPrecisionScore() >= 0.93) {
      state.setResponseProvider(ResponseProviders.L2Cache);
    } else {
      state.setIntent(intentClassificationService.getUserQueryIntent(userQuery));
      state.setResponseProviderForUserQuery();
    }

    state.setQuery(state.getResponseProvider() == ResponseProviders.L2Cache ? dto.get(0).getQuery() : userQuery);

    StrategyExecutionResult executionResult = resolveEmptyOrMissingResponse(state, userQuery);
    String response = executionResult.response();
    Long providerTokenCount = executionResult.strategy().consumeTokenCount();
    state.setTokenCount(providerTokenCount != null ? providerTokenCount : countTokensFromResponse(response));
    cacheService.saveValue(userQuery, response);
    Long queryEmbeddingId = queryEmbeddingService.storeQueryAndMetadata(userQuery, state);

    if (state.getResponseProvider() == ResponseProviders.SLM || state.getResponseProvider() == ResponseProviders.LLM) {
      docStorageRepository.save(queryEmbeddingId.toString(), response);
    }

    String mostSimilarQuery = dto.isEmpty() ? "NA" : dto.get(0).getQuery();
    return new ChatResponseDto(state.getResponseProvider().toString(), response, state.getIntent(),
        mostSimilarQuery,
        state.getPrecisionScore());
  }

  private StrategyExecutionResult resolveEmptyOrMissingResponse(ChatServiceState state, String userQuery) {
    QueryResponseStrategy strategy = queryResponseStrategyFactory.getStrategy(state.getResponseProvider());
    String response = strategy.getResponse(state.getQuery());

    if (response != null && !response.isBlank()) {
      return new StrategyExecutionResult(strategy, response);
    }

    if (state.getResponseProvider() == ResponseProviders.L2Cache) {
      state.setIntent(intentClassificationService.getUserQueryIntent(userQuery));
      state.setResponseProviderForUserQuery();
      state.setQuery(userQuery);

      QueryResponseStrategy liveStrategy = queryResponseStrategyFactory.getStrategy(state.getResponseProvider());
      String liveResponse = liveStrategy.getResponse(state.getQuery());
      return new StrategyExecutionResult(liveStrategy, liveResponse == null ? "" : liveResponse);
    }

    return new StrategyExecutionResult(strategy, response == null ? "" : response);
  }

  private long countTokensFromResponse(String response) {
    String normalizedResponse = response.trim();
    return normalizedResponse.isEmpty() ? 0 : normalizedResponse.split("\\s+").length;
  }

  private record StrategyExecutionResult(QueryResponseStrategy strategy, String response) {
  }
}
