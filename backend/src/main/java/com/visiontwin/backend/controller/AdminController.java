package com.visiontwin.backend.controller;

import com.visiontwin.backend.entity.DiagnosisReport;
import com.visiontwin.backend.repository.DiagnosisReportRepository;
import com.visiontwin.backend.repository.KnowledgeBaseLayer1Repository;
import com.visiontwin.backend.repository.KnowledgeBaseLayer2Repository;
import com.visiontwin.backend.repository.MachineRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final MachineRepository machineRepository;
    private final DiagnosisReportRepository reportRepository;
    private final KnowledgeBaseLayer1Repository layer1Repository;
    private final KnowledgeBaseLayer2Repository layer2Repository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        Map<String, Object> response = new HashMap<>();
        if ("admin".equals(request.getUsername()) && "pass123".equals(request.getPassword())) {
            response.put("success", true);
            response.put("token", "mock-admin-session-token");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Invalid username or password");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalMachines", machineRepository.count());
        stats.put("totalReports", reportRepository.count());
        stats.put("totalLayer1Datastores", layer1Repository.count());
        stats.put("totalLayer2Vectors", layer2Repository.count());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/reports")
    public ResponseEntity<List<DiagnosisReport>> getAllReports() {
        return ResponseEntity.ok(reportRepository.findAllByOrderByTimestampDesc());
    }

    @GetMapping("/reports/{id}")
    public ResponseEntity<DiagnosisReport> getReportDetails(@PathVariable UUID id) {
        return reportRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}
