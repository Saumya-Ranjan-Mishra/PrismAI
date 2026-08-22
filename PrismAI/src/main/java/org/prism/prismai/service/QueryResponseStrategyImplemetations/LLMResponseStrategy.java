package org.prism.prismai.service.QueryResponseStrategyImplemetations;

import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;
import org.springframework.stereotype.Component;

@Component
public class LLMResponseStrategy implements QueryResponseStrategy {

  @Override
  public ResponseProviders provider() {
    return ResponseProviders.LLM;
  }

  @Override
  public String getResponse(String userQuery) {
    return "returned from LLM";
  }
}
