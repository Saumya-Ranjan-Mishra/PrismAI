package org.prism.prismai.service.QueryResponseStrategyImplemetations;

import java.util.List;

import org.prism.prismai.DTO.ChatRequestDto;
import org.prism.prismai.DTO.UserQueryMessage;
import org.prism.prismai.entities.ResponseProviders;
import org.prism.prismai.service.interfaces.QueryResponseStrategy;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SLMResponseStrategy implements QueryResponseStrategy {

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
    return restTemplate
        .postForEntity("http://localhost:11434/v1/chat/completions", getRequestDto(userQuery), String.class).getBody();
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

}
