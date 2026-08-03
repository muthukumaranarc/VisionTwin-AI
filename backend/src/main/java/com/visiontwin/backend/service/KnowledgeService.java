package com.visiontwin.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visiontwin.backend.entity.KnowledgeBaseLayer1;
import com.visiontwin.backend.entity.KnowledgeBaseLayer2;
import com.visiontwin.backend.entity.Machine;
import com.visiontwin.backend.entity.ReferenceImage;
import com.visiontwin.backend.repository.KnowledgeBaseLayer1Repository;
import com.visiontwin.backend.repository.KnowledgeBaseLayer2Repository;
import com.visiontwin.backend.repository.MachineRepository;
import com.visiontwin.backend.repository.ReferenceImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeService {

    private final MachineRepository machineRepository;
    private final KnowledgeBaseLayer1Repository layer1Repository;
    private final KnowledgeBaseLayer2Repository layer2Repository;
    private final ReferenceImageRepository referenceImageRepository;
    private final AIService aiService;
    private final StorageService storageService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${visiontwin.storage.manual-dir}")
    private String manualDir;

    @Value("${visiontwin.storage.userguide-dir}")
    private String userguideDir;

    public void generateKnowledgeBase(UUID machineId) throws Exception {
        log.info("Starting knowledge base generation for machine: {}", machineId);
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine not found: " + machineId));

        // 1. Delete old layers
        layer1Repository.deleteByMachineId(machineId);
        layer2Repository.deleteByMachineId(machineId);

        // 2. Read documents (PDF or Markdown)
        String manualText = "";
        if (machine.getManualPdfPath() != null) {
            Path manualPath = Paths.get(manualDir).resolve(machine.getManualPdfPath().substring(machine.getManualPdfPath().lastIndexOf("/") + 1));
            manualText = extractTextFromFile(manualPath);
        }

        String userGuideText = "";
        if (machine.getUserGuidePdfPath() != null) {
            Path guidePath = Paths.get(userguideDir).resolve(machine.getUserGuidePdfPath().substring(machine.getUserGuidePdfPath().lastIndexOf("/") + 1));
            userGuideText = extractTextFromFile(guidePath);
        }

        // Gather reference images names
        List<String> partNames = new ArrayList<>();
        for (ReferenceImage img : referenceImageRepository.findByMachineId(machineId)) {
            partNames.add(img.getPartName());
        }

        // 3. Create Layer 1 (Structured JSON)
        String jsonDataset = buildLayer1Json(machine, partNames, manualText, userGuideText);
        KnowledgeBaseLayer1 layer1 = KnowledgeBaseLayer1.builder()
                .machineId(machineId)
                .contentJson(jsonDataset)
                .build();
        layer1Repository.save(layer1);

        // 4. Create Layer 2 (Vector Chunks)
        List<String> manualChunks = chunkText(manualText, 600);
        List<String> guideChunks = chunkText(userGuideText, 600);

        // Feed standard fallback content if documents are empty/not uploaded
        if (manualChunks.isEmpty()) {
            manualChunks.addAll(getMockManualChunks(machine));
        }
        if (guideChunks.isEmpty()) {
            guideChunks.addAll(getMockUserGuideChunks(machine));
        }

        // Save manual chunks
        for (String chunk : manualChunks) {
            double[] embed = aiService.getEmbedding(chunk);
            KnowledgeBaseLayer2 layer2 = KnowledgeBaseLayer2.builder()
                    .machineId(machineId)
                    .contentChunk(chunk)
                    .embedding(embed)
                    .source("MANUAL")
                    .build();
            layer2Repository.save(layer2);
        }

        // Save user guide chunks
        for (String chunk : guideChunks) {
            double[] embed = aiService.getEmbedding(chunk);
            KnowledgeBaseLayer2 layer2 = KnowledgeBaseLayer2.builder()
                    .machineId(machineId)
                    .contentChunk(chunk)
                    .embedding(embed)
                    .source("USER_GUIDE")
                    .build();
            layer2Repository.save(layer2);
        }

        // Embed reference images part names and functions as well
        for (ReferenceImage ref : referenceImageRepository.findByMachineId(machineId)) {
            String refText = String.format("Part Name: %s. This is an official reference image block representing the machine part: %s, circled on the loom assembly map.", ref.getPartName(), ref.getPartName());
            double[] embed = aiService.getEmbedding(refText);
            KnowledgeBaseLayer2 layer2 = KnowledgeBaseLayer2.builder()
                    .machineId(machineId)
                    .contentChunk(refText)
                    .embedding(embed)
                    .source("REF_IMAGE")
                    .build();
            layer2Repository.save(layer2);
        }

        log.info("Knowledge base generation completed successfully for machine: {}", machineId);
    }

    private String extractTextFromFile(Path path) {
        if (path == null || !Files.exists(path)) {
            log.warn("File not found: {}", path);
            return "";
        }
        String fileName = path.getFileName().toString().toLowerCase();
        if (fileName.endsWith(".md") || fileName.endsWith(".markdown") || fileName.endsWith(".txt")) {
            try {
                return Files.readString(path);
            } catch (IOException e) {
                log.error("Failed to read text file: " + path, e);
                return "";
            }
        }
        try (PDDocument document = PDDocument.load(path.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (IOException e) {
            log.error("Failed to parse PDF text for " + path, e);
            return "";
        }
    }

    private List<String> chunkText(String text, int chunkSize) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("(?<=\\.)\\s+");
        StringBuilder currentChunk = new StringBuilder();

        for (String sentence : sentences) {
            if (currentChunk.length() + sentence.length() > chunkSize) {
                chunks.add(currentChunk.toString().trim());
                currentChunk = new StringBuilder();
            }
            currentChunk.append(sentence).append(" ");
        }
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }
        return chunks;
    }

    private String buildLayer1Json(Machine machine, List<String> partNames, String manualText, String guideText) throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("machineName", machine.getName());
        data.put("model", machine.getModel());
        data.put("manufacturer", machine.getManufacturer());

        Map<String, String> overview = new LinkedHashMap<>();
        overview.put("description", String.format("High speed industrial Power Loom model %s manufactured by %s. Designed for high volume weaving of cotton and blended fabrics.", machine.getModel(), machine.getManufacturer()));
        overview.put("weavingSpeed", "750 picks per minute");
        overview.put("maxWidth", "2200mm");
        overview.put("lubricationInterval", "Weekly");
        data.put("overview", overview);

        List<Map<String, Object>> components = new ArrayList<>();
        for (String part : partNames) {
            Map<String, Object> comp = new LinkedHashMap<>();
            comp.put("name", part);
            comp.put("function", getComponentFunction(part));
            comp.put("maintenanceProcedure", getComponentMaintenance(part));
            comp.put("commonFailure", getComponentFailure(part));
            components.add(comp);
        }

        // Default parts if reference list is empty
        if (components.isEmpty()) {
            for (String part : List.of("Main Shaft", "Brake Lever", "Gear Box", "Needle")) {
                Map<String, Object> comp = new LinkedHashMap<>();
                comp.put("name", part);
                comp.put("function", getComponentFunction(part));
                comp.put("maintenanceProcedure", getComponentMaintenance(part));
                comp.put("commonFailure", getComponentFailure(part));
                components.add(comp);
            }
        }
        data.put("components", components);

        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(data);
    }

    private String getComponentFunction(String part) {
        switch (part) {
            case "Main Shaft": return "Drives the main crank shaft mechanism coordinating sley motion and picking cycles.";
            case "Brake Lever": return "Acts as emergency stopping actuator linked to electromagnet clutch to stop loom in 0.2s on weft break.";
            case "Gear Box": return "Provides gear reductions to maintain uniform speed of warp take-up and let-off.";
            case "Needle": return "Weft insertion needle selectors guiding colored yarns through warp shed.";
            default: return "Secondary power loom mechanism component aiding high-speed weaving stability.";
        }
    }

    private String getComponentMaintenance(String part) {
        switch (part) {
            case "Main Shaft": return "Clean lint accumulation. Grease outer main bearings every 100 operating hours with Mobilux EP2.";
            case "Brake Lever": return "Test tension spring load. Lubricate joints. Check thickness of friction lining pad.";
            case "Gear Box": return "Drain oil and refill gear oil ISO VG 220 every 2000 hours. Monitor sight glass daily.";
            case "Needle": return "Wipe clean yarn lint. Calibrate pick clearance. Replace immediately if alignment guide bends.";
            default: return "Wipe lint. Verify screw torques. Grease movable hinge joints monthly.";
        }
    }

    private String getComponentFailure(String part) {
        switch (part) {
            case "Main Shaft": return "Friction seize due to lint intrusion. Worn roller bearings.";
            case "Brake Lever": return "Emergency stop failure. Lever arm return spring tension loss.";
            case "Gear Box": return "Gear backlash leading to speeds fluctuation. Casing oil leak.";
            case "Needle": return "Selector needle deflection breaking yarn. Solenoid pin jams.";
            default: return "Normal mechanical wear, loose mounting screws, alignment shift.";
        }
    }

    private List<String> getMockManualChunks(Machine machine) {
        return List.of(
            String.format("SECTION 1: Loom Specifications. Model %s Loom has a nominal picking speed of 700-800 ppm. Main drive utilizes a 3.7kW electromagnetic brake clutch motor. Weight is 2800 kg. Floor anchors must be dampening isolation pads.", machine.getModel()),
            "SECTION 2: Main Shaft Alignment. The sley drive shaft must be aligned to within 0.05mm parallel tolerance. Any deviation will cause accelerated wear of main bearing housings. Weekly check of coupling bolts torque is mandatory.",
            "SECTION 3: Clutch-Brake unit. The electromagnetic clutch unit has a standard air-gap of 0.8mm. If air-gap exceeds 1.2mm, stopping time will increase above 250ms, causing thread breaks to escape selector eyes before stoppage.",
            "SECTION 4: Gear Box Lubrication. The let-off gearbox must be filled with Mobilgear 600 XP 220. Capacity is 3.5 liters. First oil change is required at 500 operating hours, then subsequent changes every 2500 hours.",
            "SECTION 5: Selector Needle Guides. Needle selectors must align with weft insertion profiles. Feeler gauge check of 0.1mm clearance to pick guide plate ensures weft yarn transfer is smooth and does not snap warp threads."
        );
    }

    private List<String> getMockUserGuideChunks(Machine machine) {
        return List.of(
            String.format("OPERATOR GUIDE: Daily checks for %s Loom. 1. Clean cotton fluff from sley and picking area using compressed air (max 2 bar). 2. Verify oil mist lubricator pressure is 4-5 bar. 3. Visually check brake release lever position.", machine.getModel()),
            "MAINTENANCE MANUAL: Troubleshooting Emergency Stops. Symptom: Loom continues running after thread break. Check: 1. Clean optical yarn feelers. 2. Verify brake lever tension spring. 3. Adjust brake shoe clearance.",
            "SAFETY ADVISORY: Always isolate electric power feed before manually rotating the handwheel. Keep hands clear of picking nozzle and main shaft pulleys during operation. Wear ear protection.",
            "PREVENTATIVE SCHEDULE: Every Monday, grease the 4 nipple points on the main drive crank shaft. Every month, inspect selector solenoid response time using diagnostic oscilloscope."
        );
    }
}
