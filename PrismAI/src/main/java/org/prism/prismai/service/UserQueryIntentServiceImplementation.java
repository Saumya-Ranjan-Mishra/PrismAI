package org.prism.prismai.service;

import org.prism.prismai.DTO.IntentClassificationRequestDto;
import org.prism.prismai.service.interfaces.IntentClassificationService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UserQueryIntentServiceImplementation implements IntentClassificationService {

  private final RestTemplate restTemplate;

  public UserQueryIntentServiceImplementation(RestTemplate template) {
    this.restTemplate = template;
  }

  @Override
  public String getUserQueryIntent(String userQuery) {
    IntentClassificationRequestDto requestDto = getIntentClassificationRequestDto(userQuery);
    return restTemplate.postForEntity("http://localhost:11434/api/generate", requestDto, String.class).getBody();
  }

  private IntentClassificationRequestDto getIntentClassificationRequestDto(String userQuery) {
    IntentClassificationRequestDto requestDto = new IntentClassificationRequestDto();
    requestDto.setFormat("string");
    requestDto.getOptions().setTemperature(0.0);
    requestDto.setModel("phi3:mini");
    requestDto.setStream(false);
    requestDto.setSystem(
        "You are an intent classification engine for an enterprise API Gateway. Your task is to analyze the USER_QUERY and determine if it is safe to serve from a static cache, or if it requires fresh dynamic execution.\n\nCLASSIFICATION RULES:\n1. STATIC_FACTUAL: The query asks for general documentation, standard policies, static definitions, or unchanged code syntax. (Safe to cache).\n2. REALTIME_DYNAMIC: The query asks for real-time status, specific dates/timestamps, user account details, live metrics, or dynamic environment states (e.g., \"Production\", \"Current\"). (NEVER cache).\n3. COMPLEX_REASONING: The query requires step-by-step logic, code debugging, or multi-faceted analysis. (NEVER cache).\n\nOUTPUT FORMAT:\nRespond ONLY with a valid String stating the intent. Do not include markdown formatting, intro, or explanation.");

    requestDto.setPrompt("USER_QUERY: \n" + userQuery);

    return requestDto;
  }

}
