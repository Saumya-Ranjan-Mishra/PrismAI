package org.prism.prismai.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IntentClassificationRequestDto {
  public String model;
  public String system;
  public String prompt;
  public boolean stream;
  public String format;
  public LLMOptions options;
}
