package org.prism.prismai.service.QueryResponseStrategyImplemetations;

import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;
import org.springframework.stereotype.Component;

@Component
public class L1CacheReponseStrategy implements QueryResponseStrategy {

  @Override
  public ResponseProviders provider() {
    return ResponseProviders.L1Cache;
  }

  @Override
  public String getResponse(String userQuery) {
    return "responded from Cache";
  }

}
