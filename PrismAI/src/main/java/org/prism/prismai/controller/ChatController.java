package org.prism.prismai.controller;

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
    public ResponseEntity<String> send(@RequestParam String message) {
        String response = chatService.serveUserQuery(message);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
