package org.prism.prismai.service;

import org.prism.prismai.DTO.IntentClassificationRequestDto;
import org.prism.prismai.DTO.LLMOptions;
import org.prism.prismai.service.interfaces.IntentClassificationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class UserQueryIntentServiceImplementation implements IntentClassificationService {

  private static final Pattern RESPONSE_PATTERN = Pattern.compile("\\\"response\\\"\\s*:\\s*\\\"(.*?)\\\"",
      Pattern.DOTALL);

  private final RestTemplate restTemplate;
  private final String ollamaBaseUrl;
  private final String model;
  private final double temperature;

  public UserQueryIntentServiceImplementation(
      RestTemplate template,
      @Value("${prism.ollama.base-url}") String ollamaBaseUrl,
      @Value("${prism.ollama.intent-model}") String model,
      @Value("${prism.ollama.intent-temperature}") double temperature) {
    this.restTemplate = template;
    this.ollamaBaseUrl = ollamaBaseUrl;
    this.model = model;
    this.temperature = temperature;
  }

  @Override
  public String getUserQueryIntent(String userQuery) {
    IntentClassificationRequestDto requestDto = getIntentClassificationRequestDto(userQuery);
    String body = restTemplate
      .postForEntity(ollamaBaseUrl + "/api/generate", requestDto, String.class)
      .getBody();
    return extractIntent(body);
  }

  private IntentClassificationRequestDto getIntentClassificationRequestDto(String userQuery) {
    IntentClassificationRequestDto requestDto = new IntentClassificationRequestDto();
    LLMOptions options = new LLMOptions();
    options.setTemperature(temperature);

    requestDto.setOptions(options);
    requestDto.setModel(model);
    requestDto.setStream(false);
    requestDto.setSystem(
        "You are an intent classification engine for an enterprise API Gateway. Your task is to analyze the USER_QUERY and determine if it is safe to serve from a static cache, or if it requires fresh dynamic execution.\n\nCLASSIFICATION RULES:\n1. STATIC_FACTUAL: The query asks for general documentation, standard policies, static definitions, or unchanged code syntax. (Safe to cache).\n2. REALTIME_DYNAMIC: The query asks for real-time status, specific dates/timestamps, user account details, live metrics, or dynamic environment states (e.g., \"Production\", \"Current\"). (NEVER cache).\n3. COMPLEX_REASONING: The query requires step-by-step logic, code debugging, or multi-faceted analysis. (NEVER cache).\n\nOUTPUT FORMAT:\nRespond ONLY with a valid String stating the intent. Do not include markdown formatting, intro, or explanation.");

    requestDto.setPrompt("USER_QUERY: \n" + userQuery);

    return requestDto;
  }

  private String extractIntent(String rawResponseBody) {
    if (rawResponseBody == null || rawResponseBody.isBlank()) {
      return "";
    }

    try {
      Matcher matcher = RESPONSE_PATTERN.matcher(rawResponseBody);
      if (matcher.find()) {
        return matcher.group(1).replace("\\n", "\n").replace("\\\"", "\"").trim();
      }
      return rawResponseBody.trim();
    } catch (Exception exception) {
      return rawResponseBody.trim();
    }
  }

}
