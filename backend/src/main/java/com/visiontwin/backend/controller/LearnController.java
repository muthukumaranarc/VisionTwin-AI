package com.visiontwin.backend.controller;

import com.visiontwin.backend.entity.LearnMessage;
import com.visiontwin.backend.service.LearnService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/learn")
@RequiredArgsConstructor
public class LearnController {

    private final LearnService learnService;

    @PostMapping("/{machineId}")
    public ResponseEntity<LearnMessage> sendMessage(
            @PathVariable UUID machineId,
            @RequestParam String sessionId,
            @RequestBody LearnRequest request) {
        try {
            LearnMessage response = learnService.sendLearnMessage(machineId, sessionId, request.getMessage(), request.getModel());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{machineId}/history")
    public ResponseEntity<List<LearnMessage>> getHistory(
            @PathVariable UUID machineId,
            @RequestParam String sessionId) {
        try {
            List<LearnMessage> history = learnService.getLearnHistory(machineId, sessionId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{machineId}")
    public ResponseEntity<Void> clearHistory(
            @PathVariable UUID machineId,
            @RequestParam String sessionId) {
        try {
            learnService.clearLearnHistory(machineId, sessionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Data
    public static class LearnRequest {
        private String message;
        private String model;
    }
}
