package org.prism.prismai.DTO;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class QueryEmbeddingDto {
  public String query;
  public BigDecimal precisionScore;
}
