package org.prism.prismai.DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestDto {
  String model;
  double temperature;
  @JsonProperty("keep_alive")
  String keepAlive;
  List<UserQueryMessage> messages;
  boolean stream;
}
