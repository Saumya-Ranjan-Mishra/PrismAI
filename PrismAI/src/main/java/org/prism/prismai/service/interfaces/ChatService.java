package org.prism.prismai.service.interfaces;

import org.prism.prismai.DTO.ChatResponseDto;

public interface ChatService {
  ChatResponseDto serveUserQuery(String userQuery);
}
