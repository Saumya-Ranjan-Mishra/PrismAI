package org.prism.prismai.service.QueryResponseStrategyImplemetations;

import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.service.interfaces.CacheService;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;
import org.springframework.stereotype.Component;

@Component
public class L2CacheResponseStrategy implements QueryResponseStrategy {
  private final CacheService cacheService;

  public L2CacheResponseStrategy(CacheService cacheService) {
    this.cacheService = cacheService;
  }

  @Override
  public ResponseProviders provider() {
    return ResponseProviders.L2Cache;
  }

  @Override
  public String getResponse(String userQuery) {
    return cacheService.getValue(userQuery);
  }

}
