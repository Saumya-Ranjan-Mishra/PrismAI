package org.prism.prismai.controller;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.concurrent.Executor;

import org.prism.prismai.DTO.ChatResponseDto;
import org.prism.prismai.service.interfaces.ChatService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final Executor chatStreamingExecutor;

    public ChatController(ChatService chatService, Executor chatStreamingExecutor) {
        this.chatService = chatService;
        this.chatStreamingExecutor = chatStreamingExecutor;
    }

    @GetMapping("/initiateChat")
    public ResponseEntity<ChatResponseDto> send(@RequestParam String message) {
        ChatResponseDto response = chatService.serveUserQuery(message);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = "/initiateChat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@RequestParam String message) {
        SseEmitter emitter = new SseEmitter(0L);
        chatStreamingExecutor.execute(() -> {
            try {
                ChatResponseDto response = chatService.streamUserQuery(message, chunk -> {
                    try {
                        emitter.send(SseEmitter.event().name("token").data(Map.of("content", chunk)));
                    } catch (IOException exception) {
                        throw new UncheckedIOException(exception);
                    }
                });
                emitter.send(SseEmitter.event().name("complete").data(response));
                emitter.complete();
            } catch (Exception exception) {
                try {
                    emitter.send(SseEmitter.event().name("error").data("Unable to generate response."));
                    emitter.complete();
                } catch (IOException sendException) {
                    emitter.completeWithError(sendException);
                }
            }
        });
        return emitter;
    }
}
