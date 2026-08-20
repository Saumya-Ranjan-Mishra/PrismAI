package org.prism.prismai.service.interfaces;

import org.prism.prismai.entities.ResponseProviders;

public interface QueryResponseStrategyFactory {
  QueryResponseStrategy getStrategy(ResponseProviders responseProviders);
}
