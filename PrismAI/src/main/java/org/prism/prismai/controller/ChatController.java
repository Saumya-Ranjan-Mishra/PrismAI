package org.prism.prismai.controller;

import org.prism.prismai.DTO.ChatResponseDto;
import org.prism.prismai.service.interfaces.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @GetMapping("/initiateChat")
    public ResponseEntity<ChatResponseDto> send(@RequestParam String message) {
        ChatResponseDto response = chatService.serveUserQuery(message);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
