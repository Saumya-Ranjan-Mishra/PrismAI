package org.prism.prismai.service.QueryResponseStrategyImplemetations;

import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;
import org.springframework.stereotype.Component;

@Component
public class L2CacheResponseStrategy implements QueryResponseStrategy {

  @Override
  public ResponseProviders provider() {
    return ResponseProviders.L2Cache;
  }

  @Override
  public String getResponse(String userQuery) {
    return "returned from L2 Cache";
  }

}
