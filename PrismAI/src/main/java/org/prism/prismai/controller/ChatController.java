package org.prism.prismai.controller;

import org.prism.prismai.services.interfaces.QueryEmbeddingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final QueryEmbeddingService queryEmbeddingService;

    public ChatController(QueryEmbeddingService queryEmbeddingService) {
        this.queryEmbeddingService = queryEmbeddingService;
    }

    @GetMapping("/initiateChat")
    public ResponseEntity<String> send(@RequestParam String message) {
        queryEmbeddingService.storeQuery(message);
        return ResponseEntity.status(HttpStatus.OK).body("question received successfully");
    }
}
