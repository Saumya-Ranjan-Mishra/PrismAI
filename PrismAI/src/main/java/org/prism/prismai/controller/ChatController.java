package org.prism.prismai.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @GetMapping
    public ResponseEntity<String> send(@RequestParam String message)
    {
        return ResponseEntity.status(HttpStatus.OK).body("question received successfully");
    }
}
