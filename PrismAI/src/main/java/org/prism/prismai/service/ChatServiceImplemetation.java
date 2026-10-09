package org.prism.prismai.service;

import java.util.List;
import java.util.function.Consumer;

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
    return serveUserQuery(userQuery, null);
  }

  @Override
  public ChatResponseDto streamUserQuery(String userQuery, Consumer<String> onChunk) {
    return serveUserQuery(userQuery, onChunk);
  }

  private ChatResponseDto serveUserQuery(String userQuery, Consumer<String> onChunk) {
    ChatServiceState state = new ChatServiceState();

    String responseFromCache = cacheService.getValue(userQuery);

    if (responseFromCache != null) {
      emit(onChunk, responseFromCache);
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

    StrategyExecutionResult executionResult = resolveEmptyOrMissingResponse(state, userQuery, onChunk);
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

  private StrategyExecutionResult resolveEmptyOrMissingResponse(
      ChatServiceState state, String userQuery, Consumer<String> onChunk) {
    QueryResponseStrategy strategy = queryResponseStrategyFactory.getStrategy(state.getResponseProvider());
    String response = getResponse(strategy, state.getQuery(), onChunk);

    if (response != null && !response.isBlank()) {
      return new StrategyExecutionResult(strategy, response);
    }

    if (state.getResponseProvider() == ResponseProviders.L2Cache) {
      state.setIntent(intentClassificationService.getUserQueryIntent(userQuery));
      state.setResponseProviderForUserQuery();
      state.setQuery(userQuery);

      QueryResponseStrategy liveStrategy = queryResponseStrategyFactory.getStrategy(state.getResponseProvider());
      String liveResponse = getResponse(liveStrategy, state.getQuery(), onChunk);
      return new StrategyExecutionResult(liveStrategy, liveResponse == null ? "" : liveResponse);
    }

    return new StrategyExecutionResult(strategy, response == null ? "" : response);
  }

  private String getResponse(QueryResponseStrategy strategy, String query, Consumer<String> onChunk) {
    return onChunk == null ? strategy.getResponse(query) : strategy.getResponse(query, onChunk);
  }

  private void emit(Consumer<String> onChunk, String response) {
    if (onChunk != null && response != null && !response.isEmpty()) {
      onChunk.accept(response);
    }
  }

  private long countTokensFromResponse(String response) {
    String normalizedResponse = response.trim();
    return normalizedResponse.isEmpty() ? 0 : normalizedResponse.split("\\s+").length;
  }

  private record StrategyExecutionResult(QueryResponseStrategy strategy, String response) {
  }
}
