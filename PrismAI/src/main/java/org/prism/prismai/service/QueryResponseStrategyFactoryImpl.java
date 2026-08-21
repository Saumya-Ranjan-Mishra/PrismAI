package org.prism.prismai.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.exception.InvalidResponseProviderException;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;
import org.prism.prismai.service.interfaces.QueryResponseStrategyFactory;
import org.springframework.stereotype.Component;

@Component
public class QueryResponseStrategyFactoryImpl implements QueryResponseStrategyFactory {

  private Map<ResponseProviders, QueryResponseStrategy> strategies = new HashMap<>();

  public QueryResponseStrategyFactoryImpl(List<QueryResponseStrategy> responseStrategies) {
    this.strategies = responseStrategies.stream().collect(
        Collectors.toMap(QueryResponseStrategy::provider, strategy -> strategy));
  }

  @Override
  public QueryResponseStrategy getStrategy(ResponseProviders responseProvider) {
    QueryResponseStrategy strategy = strategies.get(responseProvider);

    if (strategy == null) {
      throw new InvalidResponseProviderException(
          "there is no response provider registered with name " + responseProvider.toString());
    }

    return strategy;
  }

}
