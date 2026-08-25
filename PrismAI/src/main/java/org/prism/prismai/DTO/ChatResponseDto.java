package org.prism.prismai.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ChatResponseDto {
  public String servedFrom;
  public String response;
  public String userIntent;
  public String closestQuery;
  public double similarityScore;
}
