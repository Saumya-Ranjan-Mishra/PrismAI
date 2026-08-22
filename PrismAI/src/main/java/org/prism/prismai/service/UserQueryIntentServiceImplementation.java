package org.prism.prismai.service;

import org.prism.prismai.service.interfaces.IntentClassificationService;
import org.springframework.stereotype.Service;

@Service
public class UserQueryIntentServiceImplementation implements IntentClassificationService {

  @Override
  public String getUserQueryIntent(String userQuery) {
    return "STATIC_FACTUAL";
  }

}
