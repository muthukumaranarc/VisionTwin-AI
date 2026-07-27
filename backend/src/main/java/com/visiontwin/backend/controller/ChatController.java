package com.visiontwin.backend.controller;

import com.visiontwin.backend.entity.ChatMessage;
import com.visiontwin.backend.service.ChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/{reportId}")
    public ResponseEntity<ChatMessage> sendMessage(
            @PathVariable UUID reportId,
            @RequestBody MessageRequest request) {
        try {
            ChatMessage response = chatService.sendMessage(reportId, request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{reportId}/history")
    public ResponseEntity<List<ChatMessage>> getChatHistory(@PathVariable UUID reportId) {
        return ResponseEntity.ok(chatService.getChatHistory(reportId));
    }

    @Data
    public static class MessageRequest {
        private String message;
    }
}
