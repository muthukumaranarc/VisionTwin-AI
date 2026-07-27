package com.visiontwin.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.visiontwin.backend.entity.*;
import com.visiontwin.backend.repository.DiagnosisReportRepository;
import com.visiontwin.backend.repository.KnowledgeBaseLayer1Repository;
import com.visiontwin.backend.repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final MachineRepository machineRepository;
    private final DiagnosisReportRepository reportRepository;
    private final KnowledgeBaseLayer1Repository layer1Repository;
    private final VectorStoreService vectorStoreService;
    private final AIService aiService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Executes the retrieval pipeline and returns the final diagnosis report.
     */
    public DiagnosisReport analyze(UUID machineId, MultipartFile imageFile, String problemDescription) throws Exception {
        log.info("Starting diagnosis analysis for machine: {}", machineId);
        
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine not found: " + machineId));

        // 1. Store the uploaded image
        String imagePathStr = storageService.storeFile(imageFile, "uploads");
        Path imagePath = storageService.loadFile(imagePathStr.substring(imagePathStr.lastIndexOf("/") + 1), "uploads");

        // 2. Call Vision AI
        String visionJson = aiService.analyzeImage(imagePath, problemDescription);
        log.info("Vision AI output: {}", visionJson);

        String partName = "";
        String damageDesc = "";
        try {
            JsonNode visionNode = objectMapper.readTree(visionJson);
            partName = visionNode.path("partName").asText("");
            damageDesc = visionNode.path("damageDescription").asText("");
        } catch (Exception e) {
            log.error("Failed to parse Vision AI JSON output", e);
        }

        // 3. Search Layer 1 (Structured Dataset JSON)
        StringBuilder retrievedContext = new StringBuilder();
        Optional<KnowledgeBaseLayer1> layer1Opt = layer1Repository.findByMachineId(machineId);
        boolean layer1Found = false;
        
        if (layer1Opt.isPresent()) {
            String jsonContent = layer1Opt.get().getContentJson();
            try {
                JsonNode l1Node = objectMapper.readTree(jsonContent);
                JsonNode componentsNode = l1Node.path("components");
                if (componentsNode.isArray()) {
                    for (JsonNode comp : componentsNode) {
                        String compName = comp.path("name").asText("");
                        if (compName.equalsIgnoreCase(partName) || partName.toLowerCase().contains(compName.toLowerCase())) {
                            retrievedContext.append("LAYER 1 MATCH:\n")
                                    .append("Component: ").append(compName).append("\n")
                                    .append("Function: ").append(comp.path("function").asText()).append("\n")
                                    .append("Maintenance: ").append(comp.path("maintenanceProcedure").asText()).append("\n")
                                    .append("Failure Mode: ").append(comp.path("commonFailure").asText()).append("\n\n");
                            layer1Found = true;
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error parsing Layer 1 content", e);
            }
        }

        // 4. Search Layer 2 (Vector Database) - always search or fallback to Layer 2 if Layer 1 didn't yield enough
        List<KnowledgeBaseLayer2> vectorMatches = vectorStoreService.search(machineId, problemDescription + " " + damageDesc, 3);
        retrievedContext.append("LAYER 2 SEMANTIC MATCHES:\n");
        for (KnowledgeBaseLayer2 match : vectorMatches) {
            retrievedContext.append("- [").append(match.getSource()).append("]: ").append(match.getContentChunk()).append("\n");
        }

        // 5. Run Reasoning Engine
        String reasoningJson = aiService.performReasoning(visionJson, retrievedContext.toString(), "[]");
        log.info("Reasoning AI output: {}", reasoningJson);

        String problem = "Unknown Machine Issue";
        String solution = "Please contact factory maintenance team.";
        String explanation = "";
        float highlightX = 0.5f;
        float highlightY = 0.5f;
        float highlightRadius = 0.15f;

        try {
            JsonNode reasoningNode = objectMapper.readTree(reasoningJson);
            problem = reasoningNode.path("problem").asText(problem);
            solution = reasoningNode.path("solution").asText(solution);
            explanation = reasoningNode.path("explanation").asText(explanation);
            highlightX = (float) reasoningNode.path("highlightX").asDouble(highlightX);
            highlightY = (float) reasoningNode.path("highlightY").asDouble(highlightY);
            highlightRadius = (float) reasoningNode.path("highlightRadius").asDouble(highlightRadius);
        } catch (Exception e) {
            log.error("Failed to parse Reasoning Engine output", e);
        }

        // 6. Save Diagnosis Report
        DiagnosisReport report = DiagnosisReport.builder()
                .machineId(machineId)
                .machineName(machine.getName())
                .problemDescription(problemDescription)
                .uploadedImagePath(imagePathStr)
                .diagnosisProblem(problem)
                .diagnosisSolution(solution)
                .highlightX(highlightX)
                .highlightY(highlightY)
                .highlightRadius(highlightRadius)
                .build();

        return reportRepository.save(report);
    }
}
