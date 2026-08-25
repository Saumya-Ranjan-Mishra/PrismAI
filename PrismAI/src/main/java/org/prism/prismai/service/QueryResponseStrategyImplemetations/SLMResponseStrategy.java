package org.prism.prismai.service.QueryResponseStrategyImplemetations;

import java.util.List;

import org.prism.prismai.DTO.ChatRequestDto;
import org.prism.prismai.DTO.OllamaChatCompletionResponseDto;
import org.prism.prismai.DTO.UserQueryMessage;
import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SLMResponseStrategy implements QueryResponseStrategy {

  private static final ThreadLocal<Long> LAST_TOTAL_TOKENS = new ThreadLocal<>();

  private final RestTemplate restTemplate;

  public SLMResponseStrategy(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @Override
  public ResponseProviders provider() {
    return ResponseProviders.SLM;
  }

  @Override
  public String getResponse(String userQuery) {
    OllamaChatCompletionResponseDto completion = restTemplate
        .postForEntity(
            "http://localhost:11434/v1/chat/completions",
            getRequestDto(userQuery),
            OllamaChatCompletionResponseDto.class)
        .getBody();

    LAST_TOTAL_TOKENS.set(extractTotalTokens(completion));

    return extractAssistantContent(completion);
  }

  @Override
  public Long consumeTokenCount() {
    Long tokenCount = LAST_TOTAL_TOKENS.get();
    LAST_TOTAL_TOKENS.remove();
    return tokenCount;
  }

  private ChatRequestDto getRequestDto(String userQuery) {
    ChatRequestDto requestDto = new ChatRequestDto();

    requestDto.setModel("phi3:mini");
    requestDto.setTemperature(0.5);

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

}
