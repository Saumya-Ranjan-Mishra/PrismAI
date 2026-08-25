package org.prism.prismai.service;

import java.util.List;

import org.prism.prismai.DTO.ChatResponseDto;
import org.prism.prismai.DTO.QueryEmbeddingDto;
import org.prism.prismai.entities.ResponseProviders;
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

  public ChatServiceImplemetation(CacheService cacheService,
      QueryResponseStrategyFactory queryResponseStrategyFactory, QueryEmbeddingService queryEmbeddingService,
      IntentClassificationService intentClassificationService) {
    this.queryEmbeddingService = queryEmbeddingService;
    this.cacheService = cacheService;
    this.queryResponseStrategyFactory = queryResponseStrategyFactory;
    this.intentClassificationService = intentClassificationService;
  }

  @Override
  public ChatResponseDto serveUserQuery(String userQuery) {
    ChatServiceState state = new ChatServiceState();

    String responseFromCache = cacheService.getValue(userQuery);

    if (responseFromCache != null)
      return new ChatResponseDto("L1Cache", responseFromCache, "NA");

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

    QueryResponseStrategy strategy = queryResponseStrategyFactory.getStrategy(state.getResponseProvider());
    String response = strategy.getResponse(state.getQuery());
    state.setTokenCount(response.split(" ").length);
    cacheService.saveValue(userQuery, response);
    queryEmbeddingService.storeQueryAndMetadata(userQuery, state);

    return new ChatResponseDto(state.getResponseProvider().toString(), response, state.getIntent());
  }
}
