package org.prism.prismai.service.interfaces;

import org.prism.prismai.DTO.ChatResponseDto;
import java.util.function.Consumer;

public interface ChatService {
  ChatResponseDto serveUserQuery(String userQuery);

  ChatResponseDto streamUserQuery(String userQuery, Consumer<String> onChunk);
}
