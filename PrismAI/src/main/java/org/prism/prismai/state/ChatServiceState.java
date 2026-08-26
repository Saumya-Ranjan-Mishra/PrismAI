package org.prism.prismai.state;

import org.prism.prismai.entities.ResponseProviders;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatServiceState {
  double precisionScore;
  String intent;
  ResponseProviders responseProvider;
  String query;
  long tokenCount;

  public void setResponseProviderForUserQuery() {
    if (this.precisionScore >= 0.80 && this.precisionScore < 0.93) {
      responseProvider = "STATIC_FACTUAL".equalsIgnoreCase(intent) ? ResponseProviders.L2Cache
          : ResponseProviders.SLM;
    } else if ("STATIC_FACTUAL".equalsIgnoreCase(this.intent) || "REALTIME_DYNAMIC".equalsIgnoreCase(this.intent)) {
      responseProvider = ResponseProviders.SLM;
    } else {
      responseProvider = ResponseProviders.LLM;
    }
  }
}
