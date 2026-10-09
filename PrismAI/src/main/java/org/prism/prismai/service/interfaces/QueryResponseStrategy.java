package org.prism.prismai.service.interfaces;

import org.prism.prismai.entities.ResponseProviders;
import java.util.function.Consumer;

public interface QueryResponseStrategy {
  public ResponseProviders provider();

  public String getResponse(String userQuery);

  default String getResponse(String userQuery, Consumer<String> onChunk) {
    String response = getResponse(userQuery);
    if (response != null && !response.isEmpty()) {
      onChunk.accept(response);
    }
    return response;
  }

  default Long consumeTokenCount() {
    return null;
  }
}
