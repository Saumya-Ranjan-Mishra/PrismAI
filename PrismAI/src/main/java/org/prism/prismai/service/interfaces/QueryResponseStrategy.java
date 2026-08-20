package org.prism.prismai.service.interfaces;

import org.prism.prismai.entities.ResponseProviders;

public interface QueryResponseStrategy {
  public ResponseProviders provider();

  public String getResponse(String userQuery);
}
