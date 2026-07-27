package com.visiontwin.backend.controller;

import com.visiontwin.backend.entity.DiagnosisReport;
import com.visiontwin.backend.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisController {

    private final AnalysisService analysisService;

    @PostMapping(value = "/diagnose", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DiagnosisReport> diagnose(
            @RequestParam("machineId") UUID machineId,
            @RequestParam("problemDescription") String problemDescription,
            @RequestParam("image") MultipartFile image) {
        try {
            DiagnosisReport report = analysisService.analyze(machineId, image, problemDescription);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            log.error("Failed to run diagnosis pipeline", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
