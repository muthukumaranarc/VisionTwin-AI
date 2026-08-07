package com.visiontwin.backend.service;

import com.visiontwin.backend.entity.LearnMessage;
import com.visiontwin.backend.entity.Machine;
import com.visiontwin.backend.entity.KnowledgeBaseLayer2;
import com.visiontwin.backend.repository.LearnMessageRepository;
import com.visiontwin.backend.repository.MachineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LearnService {

    private final LearnMessageRepository learnMessageRepository;
    private final MachineRepository machineRepository;
    private final VectorStoreService vectorStoreService;
    private final AIService aiService;

    public List<LearnMessage> getLearnHistory(UUID machineId, String sessionId) {
        return learnMessageRepository.findByMachineIdAndSessionIdOrderByTimestampAsc(machineId, sessionId);
    }

    public void clearLearnHistory(UUID machineId, String sessionId) {
        learnMessageRepository.deleteByMachineIdAndSessionId(machineId, sessionId);
    }

    public LearnMessage sendLearnMessage(UUID machineId, String sessionId, String userMessageText, String modelOverride) {
        log.info("Sending learning chat message for machine: {}, session: {}", machineId, sessionId);
        Machine machine = machineRepository.findById(machineId)
                .orElseThrow(() -> new IllegalArgumentException("Machine not found: " + machineId));

        // 1. Save User Message
        LearnMessage userMessage = LearnMessage.builder()
                .machineId(machineId)
                .sessionId(sessionId)
                .sender("USER")
                .messageText(userMessageText)
                .timestamp(LocalDateTime.now())
                .build();
        learnMessageRepository.save(userMessage);

        // 2. Fetch semantic matches from vector database (RAG)
        List<KnowledgeBaseLayer2> matches = vectorStoreService.search(machineId, userMessageText, 4);
        StringBuilder contextBuilder = new StringBuilder();
        for (KnowledgeBaseLayer2 match : matches) {
            contextBuilder.append("- [").append(match.getSource()).append("]: ").append(match.getContentChunk()).append("\n");
        }

        // 3. Load chat history (including current user message for context)
        List<LearnMessage> history = learnMessageRepository.findByMachineIdAndSessionIdOrderByTimestampAsc(machineId, sessionId);

        // 4. Compile prompt
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are the AI Learning Assistant for the machine: ").append(machine.getName()).append("\n");
        promptBuilder.append("Model: ").append(machine.getModel()).append(", Manufacturer: ").append(machine.getManufacturer()).append("\n\n");
        promptBuilder.append("Your goal is to help factory operators and technicians study the Machine Manual and User Guide for this machine.\n");
        promptBuilder.append("Here is the relevant context retrieved from the manuals and guides:\n");
        if (contextBuilder.length() > 0) {
            promptBuilder.append(contextBuilder.toString()).append("\n");
        } else {
            promptBuilder.append("No specific documentation matches found. Use general knowledge about ").append(machine.getName()).append(".\n\n");
        }

        promptBuilder.append("Below is the conversation history with the employee:\n");
        for (LearnMessage msg : history) {
            promptBuilder.append(msg.getSender()).append(": ").append(msg.getMessageText()).append("\n");
        }
        promptBuilder.append("AI: ");

        // 5. Generate Response
        String aiResponseText = aiService.generateText(promptBuilder.toString(), modelOverride);

        // Fallback to mock if empty response (mock provider or API issue)
        if (aiResponseText == null || aiResponseText.trim().isEmpty()) {
            aiResponseText = getMockLearningResponse(machine, userMessageText);
        }

        // 6. Save and Return AI Message
        LearnMessage aiMessage = LearnMessage.builder()
                .machineId(machineId)
                .sessionId(sessionId)
                .sender("AI")
                .messageText(aiResponseText)
                .timestamp(LocalDateTime.now())
                .build();
        return learnMessageRepository.save(aiMessage);
    }

    private String getMockLearningResponse(Machine machine, String query) {
        String q = query.toLowerCase();
        if (q.contains("lubricat") || q.contains("oil") || q.contains("grease")) {
            return "According to the " + machine.getName() + " manual, lubrication is critical for high-speed operation:\n\n" +
                   "• **Main Shaft:** Clean cotton lint and grease outer main bearings every 100 operating hours with Mobilux EP2.\n" +
                   "• **Gear Box:** Check oil level via sight glass daily. Change oil (ISO VG 220, capacity 3.5 liters) every 2500 hours.\n" +
                   "• **Emergency Steps:** Always shut off power completely before applying any grease or oil.";
        }
        if (q.contains("safety") || q.contains("danger") || q.contains("emergency") || q.contains("stop")) {
            return "Safety is the number one priority when working with the " + machine.getName() + ":\n\n" +
                   "• **Power Isolation:** Always turn off and lock out the electric power feed before manually rotating the handwheel or opening panels.\n" +
                   "• **Clutch-Brake:** The loom uses an electromagnetic brake motor. If thread breaks occur, the brake lever stops the loom automatically in 0.2 seconds.\n" +
                   "• **PPE:** Keep hands clear of picking nozzles and pulleys, and always wear ear protection due to high noise levels.";
        }
        if (q.contains("needle") || q.contains("weft") || q.contains("yarn")) {
            return "For weft selectors and needle calibration on the " + machine.getName() + ":\n\n" +
                   "• **Cleaning:** Blow off lint with compressed air (max 2 bar) daily.\n" +
                   "• **Replacement:** Release the selector guide screw, insert the new needle, check for a 0.1mm clearance to the guide plate with a feeler gauge, and tighten lock screw to 4 Nm.\n" +
                   "• **Issues:** Needle deflection can cause thread breakage. If warp threads snap, check needle straightness immediately.";
        }
        return "I can help you learn about the user guides and manual for the " + machine.getName() + " (" + machine.getModel() + ").\n\n" +
               "Try asking me about:\n" +
               "• Daily maintenance schedules\n" +
               "• Safety recommendations\n" +
               "• Emergency stopping mechanisms\n" +
               "• Selector needle replacement procedures";
    }
}
