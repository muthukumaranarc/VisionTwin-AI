package com.visiontwin.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visiontwin.backend.entity.ChatMessage;
import com.visiontwin.backend.entity.DiagnosisReport;
import com.visiontwin.backend.repository.ChatMessageRepository;
import com.visiontwin.backend.repository.DiagnosisReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final DiagnosisReportRepository reportRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AIService aiService;

    public ChatMessage sendMessage(UUID reportId, String userMessageText) {
        log.info("Sending chat message for report: {}", reportId);
        DiagnosisReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Diagnosis report not found: " + reportId));

        // 1. Save User Message
        ChatMessage userMessage = ChatMessage.builder()
                .reportId(report.getId())
                .sender("USER")
                .messageText(userMessageText)
                .build();
        chatMessageRepository.save(userMessage);

        // 2. Load complete chat history for prompt
        List<ChatMessage> history = chatMessageRepository.findByReportIdOrderByTimestampAsc(reportId);

        // 3. Compile prompt context
        StringBuilder promptBuilder = new StringBuilder();
        promptBuilder.append("You are the AI assistant helping a machine operator resolve a problem with a Power Loom.\n");
        promptBuilder.append("Here is the context of the machine and the initial diagnosis:\n");
        promptBuilder.append("- Machine: ").append(report.getMachineName()).append("\n");
        promptBuilder.append("- Operator Description of Problem: ").append(report.getProblemDescription()).append("\n");
        promptBuilder.append("- Diagnosed Problem: ").append(report.getDiagnosisProblem()).append("\n");
        promptBuilder.append("- Recommended Solution: ").append(report.getDiagnosisSolution()).append("\n\n");
        
        promptBuilder.append("Below is the conversation history with the operator:\n");
        for (ChatMessage msg : history) {
            promptBuilder.append(msg.getSender()).append(": ").append(msg.getMessageText()).append("\n");
        }
        promptBuilder.append("AI: [Generate your response here, keeping it extremely simple, concise, friendly, and practical for a factory operator. Keep it under 3-4 sentences. Do not use technical jargon.]");

        // 4. Generate AI response
        String aiResponseText;
        if ("mock".equalsIgnoreCase(aiService.getEmbedding("test") != null ? "mock" : "real")) { // Simple check
            aiResponseText = getMockChatResponse(report.getDiagnosisProblem(), userMessageText);
        } else {
            try {
                if ("openai".equalsIgnoreCase(System.getenv("AI_PROVIDER"))) {
                    aiResponseText = aiService.performReasoning("{}", promptBuilder.toString(), "[]");
                } else {
                    aiResponseText = aiService.performReasoning("{}", promptBuilder.toString(), "[]");
                }
                // Strip JSON if the service returned it in JSON form or extract reasoning fields.
                // To guarantee clean, simple text responses, let's parse if JSON, otherwise use as-is.
                if (aiResponseText.trim().startsWith("{")) {
                    ObjectMapper mapper = new ObjectMapper();
                    aiResponseText = mapper.readTree(aiResponseText).path("explanation").asText(aiResponseText);
                }
            } catch (Exception e) {
                log.error("AI response failed, using mock chat response", e);
                aiResponseText = getMockChatResponse(report.getDiagnosisProblem(), userMessageText);
            }
        }

        // 5. Save and Return AI Message
        ChatMessage aiMessage = ChatMessage.builder()
                .reportId(report.getId())
                .sender("AI")
                .messageText(aiResponseText)
                .build();
        return chatMessageRepository.save(aiMessage);
    }

    public List<ChatMessage> getChatHistory(UUID reportId) {
        return chatMessageRepository.findByReportIdOrderByTimestampAsc(reportId);
    }

    private String getMockChatResponse(String diagnosisProblem, String query) {
        String q = query.toLowerCase();
        if (q.contains("cause") || q.contains("why")) {
            if (diagnosisProblem.contains("Shaft")) {
                return "This is usually caused by cotton dust getting trapped in the bearing seals over time, which dries out the oil. Regular blowing with air and weekly oiling prevents this.";
            } else if (diagnosisProblem.contains("Needle")) {
                return "Needle alignment issues are usually caused by yarn tension surges or micro-shocks when weaving heavier fabrics. Resetting the solenoid guide solves it.";
            } else {
                return "Friction and lint buildup during high-speed operation are the primary causes. Regular daily cleaning will prevent this issue.";
            }
        }
        if (q.contains("first") || q.contains("start")) {
            return "First, make sure the machine is completely powered off and safety locks are in place! Safety is most important. Then, proceed with cleaning the lint before applying any oil.";
        }
        if (q.contains("use") || q.contains("continue") || q.contains("run")) {
            return "No, you should not run the machine right now. Operating with this fault can break the weft yarn or damage the motor. Please solve the issue first.";
        }
        return "I suggest double-checking the repair steps I provided. Make sure to clean the area thoroughly. If the problem persists, notify the maintenance supervisor.";
    }
}
