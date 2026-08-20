package org.prism.prismai.service.QueryResponseStrategyImplemetations;

import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;

public class SLMResponseStrategy implements QueryResponseStrategy {

  @Override
  public ResponseProviders provider() {
    return ResponseProviders.LLM;
  }

  @Override
  public String getResponse(String userQuery) {
    return "returned from SLM";
  }

}
