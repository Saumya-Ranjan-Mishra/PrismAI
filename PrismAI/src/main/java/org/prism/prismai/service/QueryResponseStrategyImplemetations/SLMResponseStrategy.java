package org.prism.prismai.service.QueryResponseStrategyImplemetations;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.prism.prismai.DTO.ChatRequestDto;
import org.prism.prismai.DTO.OllamaChatCompletionResponseDto;
import org.prism.prismai.DTO.UserQueryMessage;
import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SLMResponseStrategy implements QueryResponseStrategy {

  private static final ThreadLocal<Long> LAST_TOTAL_TOKENS = new ThreadLocal<>();

  private final RestTemplate restTemplate;
  private final String ollamaBaseUrl;
  private final String model;
  private final String keepAlive;
  private final double temperature;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public SLMResponseStrategy(
      RestTemplate restTemplate,
      @Value("${prism.ollama.base-url}") String ollamaBaseUrl,
      @Value("${prism.ollama.chat-model}") String model,
      @Value("${prism.ollama.keep-alive}") String keepAlive,
      @Value("${prism.ollama.chat-temperature}") double temperature) {
    this.restTemplate = restTemplate;
    this.ollamaBaseUrl = ollamaBaseUrl;
    this.model = model;
    this.keepAlive = keepAlive;
    this.temperature = temperature;
  }

  @Override
  public ResponseProviders provider() {
    return ResponseProviders.SLM;
  }

  @Override
  public String getResponse(String userQuery) {
    OllamaChatCompletionResponseDto completion = restTemplate
        .postForEntity(
          ollamaBaseUrl + "/v1/chat/completions",
            getRequestDto(userQuery),
            OllamaChatCompletionResponseDto.class)
        .getBody();

    LAST_TOTAL_TOKENS.set(extractTotalTokens(completion));

    return extractAssistantContent(completion);
  }

  @Override
  public String getResponse(String userQuery, Consumer<String> onChunk) {
    LAST_TOTAL_TOKENS.remove();

    ChatRequestDto requestDto = getRequestDto(userQuery);
    requestDto.setStream(true);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setAccept(List.of(MediaType.TEXT_EVENT_STREAM));
    HttpEntity<ChatRequestDto> request = new HttpEntity<>(requestDto, headers);

    StreamedResponse streamedResponse = Objects.requireNonNullElse(
      restTemplate.execute(
        ollamaBaseUrl + "/v1/chat/completions",
        HttpMethod.POST,
        restTemplate.httpEntityCallback(request),
        response -> readStream(response.getBody(), onChunk)),
      new StreamedResponse("", null));

    LAST_TOTAL_TOKENS.set(streamedResponse.totalTokens());
    return streamedResponse.content();
  }

  @Override
  public Long consumeTokenCount() {
    Long tokenCount = LAST_TOTAL_TOKENS.get();
    LAST_TOTAL_TOKENS.remove();
    return tokenCount;
  }

  private ChatRequestDto getRequestDto(String userQuery) {
    ChatRequestDto requestDto = new ChatRequestDto();

    requestDto.setModel(model);
    requestDto.setTemperature(temperature);
    requestDto.setKeepAlive(keepAlive);

    UserQueryMessage message = new UserQueryMessage();
    message.setRole("user");
    message.setContent(userQuery);

    requestDto.setMessages(List.of(message));

    return requestDto;
  }

  private String extractAssistantContent(OllamaChatCompletionResponseDto completion) {
    if (completion == null || completion.getChoices() == null || completion.getChoices().isEmpty()) {
      return "";
    }

    OllamaChatCompletionResponseDto.Choice firstChoice = completion.getChoices().get(0);
    if (firstChoice == null || firstChoice.getMessage() == null || firstChoice.getMessage().getContent() == null) {
      return "";
    }

    return firstChoice.getMessage().getContent().trim();
  }

  private Long extractTotalTokens(OllamaChatCompletionResponseDto completion) {
    if (completion == null || completion.getUsage() == null || completion.getUsage().getTotal_tokens() == null) {
      return null;
    }
    return completion.getUsage().getTotal_tokens().longValue();
  }

  private StreamedResponse readStream(InputStream inputStream, Consumer<String> onChunk) throws IOException {
    StringBuilder responseText = new StringBuilder();
    AtomicReference<Long> totalTokens = new AtomicReference<>();

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        processSseLine(line, responseText, totalTokens, onChunk);
      }
    }

    return new StreamedResponse(responseText.toString().trim(), totalTokens.get());
  }

  private void processSseLine(
      String line, StringBuilder responseText, AtomicReference<Long> totalTokens, Consumer<String> onChunk)
      throws IOException {
    if (!line.startsWith("data:")) {
      return;
    }

    String data = line.substring("data:".length()).trim();
    if (data.isEmpty() || "[DONE]".equals(data)) {
      return;
    }

    JsonNode event = objectMapper.readTree(data);
    JsonNode usage = event.path("usage");
    if (usage.path("total_tokens").canConvertToLong()) {
      totalTokens.set(usage.path("total_tokens").longValue());
    }

    JsonNode content = event.path("choices").path(0).path("delta").path("content");
    if (content.isTextual() && !content.asText().isEmpty()) {
      String chunk = content.asText();
      responseText.append(chunk);
      onChunk.accept(chunk);
    }
  }

  private record StreamedResponse(String content, Long totalTokens) {
  }

}
