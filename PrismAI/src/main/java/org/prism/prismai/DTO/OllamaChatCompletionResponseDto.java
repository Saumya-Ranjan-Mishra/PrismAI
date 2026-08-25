package org.prism.prismai.DTO;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OllamaChatCompletionResponseDto {
  private String id;
  private String object;
  private Long created;
  private String model;
  private String system_fingerprint;
  private List<Choice> choices;
  private Usage usage;

  @Getter
  @Setter
  public static class Choice {
    private Integer index;
    private Message message;
    private String finish_reason;
  }

  @Getter
  @Setter
  public static class Message {
    private String role;
    private String content;
  }

  @Getter
  @Setter
  public static class Usage {
    private Integer prompt_tokens;
    private Integer completion_tokens;
    private Integer total_tokens;
  }
}