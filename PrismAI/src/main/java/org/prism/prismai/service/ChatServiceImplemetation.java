package org.prism.prismai.service;

import java.util.List;
import org.prism.prismai.DTO.QueryEmbeddingDto;
import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.service.interfaces.CacheService;
import org.prism.prismai.service.interfaces.ChatService;
import org.prism.prismai.service.interfaces.IntentClassificationService;
import org.prism.prismai.service.interfaces.QueryEmbeddingService;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;
import org.prism.prismai.service.interfaces.QueryResponseStrategyFactory;
import org.springframework.stereotype.Service;

import lombok.Getter;
import lombok.Setter;

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
  public String serveUserQuery(String userQuery) {
    ChatServiceState state = new ChatServiceState();

    String responseFromCache = cacheService.getValue(userQuery);

    if (responseFromCache != null)
      return responseFromCache;

    List<QueryEmbeddingDto> dto = queryEmbeddingService.findSimilarQueries(userQuery, 1);

    if (!dto.isEmpty()) {
      state.setPrecisionScore(dto.get(0).getPrecisionScore().doubleValue());
    }

    if (state.precisionScore >= 0.93) {
      state.setResponseProvider(ResponseProviders.L2Cache);
    } else {
      state.setIntent(intentClassificationService.getUserQueryIntent(userQuery));
      state.setResponseProviderForUserQuery();
    }

    state.setQuery(state.responseProvider == ResponseProviders.L2Cache ? dto.get(0).getQuery() : userQuery);

    QueryResponseStrategy strategy = queryResponseStrategyFactory.getStrategy(state.responseProvider);
    String response = strategy.getResponse(state.query);
    cacheService.saveValue(userQuery, response);

    return response;
  }

  @Getter
  @Setter
  private class ChatServiceState {
    double precisionScore;
    String intent;
    ResponseProviders responseProvider;
    String query;

    private void setResponseProviderForUserQuery() {
      if (this.precisionScore > 0.80 && this.precisionScore < 0.93) {
        responseProvider = "STATIC_FACTUAL".equalsIgnoreCase(intent) ? ResponseProviders.L2Cache
            : ResponseProviders.SLM;
      } else if ("STATIC_FACTUAL".equalsIgnoreCase(this.intent) || "REALTIME_DYNAMIC".equalsIgnoreCase(this.intent)) {
        responseProvider = ResponseProviders.SLM;
      } else {
        responseProvider = ResponseProviders.LLM;
      }
    }
  }

}
