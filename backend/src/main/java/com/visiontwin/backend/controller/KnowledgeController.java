package com.visiontwin.backend.controller;

import com.visiontwin.backend.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    @PostMapping("/generate/{machineId}")
    public ResponseEntity<Map<String, Object>> generateKnowledgeBase(@PathVariable UUID machineId) {
        Map<String, Object> response = new HashMap<>();
        try {
            knowledgeService.generateKnowledgeBase(machineId);
            response.put("success", true);
            response.put("message", "Knowledge base layers generated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to generate knowledge base layers", e);
            response.put("success", false);
            response.put("message", "Failed to generate knowledge base: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
