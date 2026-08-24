package org.prism.prismai.DTO;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatRequestDto {
  String model;
  double temperature;
  List<UserQueryMessage> messages;
}
