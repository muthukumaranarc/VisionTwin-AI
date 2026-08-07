package com.visiontwin.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AIService {

    @Value("${visiontwin.ai.provider:mock}")
    private String provider;

    @Value("${visiontwin.ai.api-key:}")
    private String apiKey;

    @Value("${visiontwin.ai.api-url:}")
    private String apiUrl;

    @Value("${visiontwin.ai.vision-model:google/gemini-flash-1.5-exp}")
    private String visionModelName;

    @Value("${visiontwin.ai.text-model:meta-llama/llama-3.1-8b-instruct:free}")
    private String textModelName;

    @Value("${visiontwin.ai.embedding-model:text-embedding-004}")
    private String embeddingModelName;

    @Value("${visiontwin.ai.available-models:gemini-3.6-flash,gemini-2.5-pro,gemini-2.5-flash,gemini-2.0-flash}")
    private List<String> availableModels;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(60))
            .build();

    public String getDefaultModel() {
        return visionModelName;
    }

    public List<String> getAvailableModels() {
        return availableModels;
    }

    /**
     * Generate text embeddings for the given text.
     */
    public double[] getEmbedding(String text) {
        if ("mock".equalsIgnoreCase(provider) || apiKey.trim().isEmpty()) {
            return generateMockEmbedding(text);
        }

        try {
            if ("openai".equalsIgnoreCase(provider)) {
                return getOpenAIEmbedding(text);
            } else {
                return getGeminiEmbedding(text);
            }
        } catch (Exception e) {
            log.error("Error generating embedding via AI provider, falling back to mock", e);
            return generateMockEmbedding(text);
        }
    }

    /**
     * Analyze an uploaded machine image with problem description.
     * Returns a JSON string with the parsed diagnosis details:
     * - partName, description, x, y, radius
     */
    public String analyzeImage(Path imagePath, String problemDescription) {
        return analyzeImage(imagePath, problemDescription, null);
    }

    public String analyzeImage(Path imagePath, String problemDescription, String modelOverride) {
        if ("mock".equalsIgnoreCase(provider) || apiKey.trim().isEmpty()) {
            return getMockAnalysis(problemDescription);
        }

        try {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            String base64Image = Base64.getEncoder().encodeToString(imageBytes);
            String mimeType = "image/jpeg";
            if (imagePath.getFileName().toString().endsWith(".png")) {
                mimeType = "image/png";
            }

            String prompt = "You are an expert AI maintenance system for Power Loom factory machines.\n" +
                    "Analyze the uploaded machine image and the operator's problem description: \"" + problemDescription + "\".\n" +
                    "Identify the most likely faulty machine component/part visible in the image, describe the specific failure issue, and pinpoint the suspect region in the image.\n" +
                    "Return ONLY a raw JSON object with the following fields (no markdown formatting, no backticks, no other text):\n" +
                    "{\n" +
                    "  \"partName\": \"The exact official name of the detected faulty component/part (e.g. Main Shaft, Brake Lever, Gear Box, Needle)\",\n" +
                    "  \"damageDescription\": \"A detailed description of the observed or suspected visible damage and failure state\",\n" +
                    "  \"x\": 0.45, // Center X coordinate of a bounding circle highlighting the faulty part, normalized between 0.0 and 1.0\n" +
                    "  \"y\": 0.55, // Center Y coordinate, normalized between 0.0 and 1.0\n" +
                    "  \"radius\": 0.15 // Normalized radius of the highlight circle (e.g., between 0.05 and 0.3)\n" +
                    "}";

            if ("openai".equalsIgnoreCase(provider)) {
                return callOpenAIVision(base64Image, mimeType, prompt);
            } else {
                return callGeminiVision(base64Image, mimeType, prompt, modelOverride);
            }
        } catch (Exception e) {
            log.error("Error analyzing image via AI, falling back to mock", e);
            return getMockAnalysis(problemDescription);
        }
    }

    /**
     * Perform the final reasoning combining vision analysis, retrieved knowledge context, and conversation history.
     */
    public String performReasoning(String visionAnalysisJson, String retrievedContext, String conversationHistoryJson) {
        return performReasoning(visionAnalysisJson, retrievedContext, conversationHistoryJson, null);
    }

    public String performReasoning(String visionAnalysisJson, String retrievedContext, String conversationHistoryJson, String modelOverride) {
        if ("mock".equalsIgnoreCase(provider) || apiKey.trim().isEmpty()) {
            return getMockReasoning(visionAnalysisJson, retrievedContext);
        }

        try {
            String prompt = "You are the Reasoning Engine of VisionTwin AI maintenance assistant for Power Loom machines.\n" +
                    "You must formulate a final production-grade diagnosis based on these inputs:\n" +
                    "1. Vision AI Analysis: " + visionAnalysisJson + "\n" +
                    "2. Retrieved Knowledge Base Context (Manuals, Guides, Reference Parts): " + retrievedContext + "\n" +
                    "3. Conversation History (if any): " + conversationHistoryJson + "\n\n" +
                    "Formulate a friendly, operator-focused solution. Speak simply, like talking to a technician.\n" +
                    "Return ONLY a JSON response (no markdown blocks, no enclosing backticks, no comments) structured EXACTLY as follows:\n" +
                    "{\n" +
                    "  \"problem\": \"Clear, concise definition of what the failure is (e.g. Main Shaft Seizure / Gear Box Lubrication Failure)\",\n" +
                    "  \"solution\": \"Step-by-step simple instructions on how to solve this problem (bulleted plain text)\",\n" +
                    "  \"explanation\": \"A simple 2-3 sentence explanation of the cause and what it affects\",\n" +
                    "  \"highlightX\": 0.45, // Normalized X coordinate (use coordinates from Vision AI)\n" +
                    "  \"highlightY\": 0.55, // Normalized Y coordinate\n" +
                    "  \"highlightRadius\": 0.15 // Normalized radius\n" +
                    "}";

            if ("openai".equalsIgnoreCase(provider)) {
                return callOpenAIText(prompt);
            } else {
                return callGeminiText(prompt, modelOverride);
            }
        } catch (Exception e) {
            log.error("Error performing reasoning via AI, falling back to mock", e);
            return getMockReasoning(visionAnalysisJson, retrievedContext);
        }
    }

    public String generateText(String promptText) {
        return generateText(promptText, null);
    }

    public String generateText(String promptText, String modelOverride) {
        if ("mock".equalsIgnoreCase(provider) || apiKey.trim().isEmpty()) {
            return "";
        }
        try {
            if ("openai".equalsIgnoreCase(provider)) {
                return callOpenAIText(promptText);
            } else {
                return callGeminiText(promptText, modelOverride);
            }
        } catch (Exception e) {
            log.error("Error generating text via AI provider", e);
            return "";
        }
    }

    private String resolveModel(String override, String fallback) {
        if (override != null && !override.isBlank() && availableModels.contains(override)) {
            return override;
        }
        return fallback;
    }

    // --- GEMINI REST CLIENTS ---

    private double[] getGeminiEmbedding(String text) throws IOException, InterruptedException {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + embeddingModelName + ":embedContent?key=" + apiKey;
        if (!apiUrl.trim().isEmpty()) {
            endpoint = apiUrl + "/v1beta/models/" + embeddingModelName + ":embedContent?key=" + apiKey;
        }

        Map<String, Object> req = Map.of(
                "model", "models/" + embeddingModelName,
                "content", Map.of("parts", List.of(Map.of("text", text)))
        );
        String body = objectMapper.writeValueAsString(req);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini embedding failed: Status " + response.statusCode() + ", Body: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode values = root.path("embedding").path("values");
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i).asDouble();
        }
        return result;
    }

    private String callGeminiVision(String base64Image, String mimeType, String promptText, String modelOverride) throws IOException, InterruptedException {
        String modelName = resolveModel(modelOverride, visionModelName);
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;
        if (!apiUrl.trim().isEmpty()) {
            endpoint = apiUrl + "/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;
        }

        Map<String, Object> req = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(
                                Map.of("text", promptText),
                                Map.of("inlineData", Map.of(
                                        "mimeType", mimeType,
                                        "data", base64Image
                                ))
                        )
                ))
        );
        String body = objectMapper.writeValueAsString(req);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini vision call failed: Status " + response.statusCode() + ", Body: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        return cleanJsonString(text);
    }

    private String callGeminiText(String promptText, String modelOverride) throws IOException, InterruptedException {
        String modelName = resolveModel(modelOverride, textModelName);
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;
        if (!apiUrl.trim().isEmpty()) {
            endpoint = apiUrl + "/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;
        }

        Map<String, Object> req = Map.of(
                "contents", List.of(Map.of(
                        "parts", List.of(Map.of("text", promptText))
                ))
        );
        String body = objectMapper.writeValueAsString(req);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini text call failed: Status " + response.statusCode() + ", Body: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String text = root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
        return cleanJsonString(text);
    }

    // --- OPENAI REST CLIENTS ---

    private double[] getOpenAIEmbedding(String text) throws IOException, InterruptedException {
        String endpoint = "https://api.openai.com/v1/embeddings";
        if (!apiUrl.trim().isEmpty()) {
            if (apiUrl.contains("/embeddings")) {
                endpoint = apiUrl;
            } else {
                endpoint = apiUrl.replaceAll("/+$", "") + "/embeddings";
            }
        }

        Map<String, Object> req = Map.of(
                "input", text,
                "model", embeddingModelName
        );
        String body = objectMapper.writeValueAsString(req);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenAI embedding failed: Status " + response.statusCode() + ", Body: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        JsonNode values = root.path("data").get(0).path("embedding");
        double[] result = new double[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i).asDouble();
        }
        return result;
    }

    private String callOpenAIVision(String base64Image, String mimeType, String promptText) throws IOException, InterruptedException {
        String endpoint = "https://api.openai.com/v1/chat/completions";
        if (!apiUrl.trim().isEmpty()) {
            if (apiUrl.contains("/chat/completions")) {
                endpoint = apiUrl;
            } else {
                endpoint = apiUrl.replaceAll("/+$", "") + "/chat/completions";
            }
        }

        Map<String, Object> req = Map.of(
                "model", visionModelName,
                "messages", List.of(
                        Map.of("role", "user", "content", List.of(
                                Map.of("type", "text", "text", promptText),
                                Map.of("type", "image_url", "image_url", Map.of(
                                        "url", "data:" + mimeType + ";base64," + base64Image
                                ))
                        ))
                )
        );
        String body = objectMapper.writeValueAsString(req);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenAI vision failed: Status " + response.statusCode() + ", Body: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String text = root.path("choices").get(0).path("message").path("content").asText();
        return cleanJsonString(text);
    }

    private String callOpenAIText(String promptText) throws IOException, InterruptedException {
        String endpoint = "https://api.openai.com/v1/chat/completions";
        if (!apiUrl.trim().isEmpty()) {
            if (apiUrl.contains("/chat/completions")) {
                endpoint = apiUrl;
            } else {
                endpoint = apiUrl.replaceAll("/+$", "") + "/chat/completions";
            }
        }

        Map<String, Object> req = Map.of(
                "model", textModelName,
                "messages", List.of(
                        Map.of("role", "user", "content", promptText)
                )
        );
        String body = objectMapper.writeValueAsString(req);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("OpenAI text failed: Status " + response.statusCode() + ", Body: " + response.body());
        }

        JsonNode root = objectMapper.readTree(response.body());
        String text = root.path("choices").get(0).path("message").path("content").asText();
        return cleanJsonString(text);
    }

    // --- HELPERS & MOCK METHODS ---

    private String cleanJsonString(String text) {
        String cleaned = text.trim();
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }
        return cleaned.trim();
    }

    private double[] generateMockEmbedding(String text) {
        // Generate a deterministic unit vector based on hashcode
        int seed = text.hashCode();
        double[] vector = new double[128]; // standard small dimension
        double sum = 0;
        for (int i = 0; i < 128; i++) {
            vector[i] = Math.sin(seed + i);
            sum += vector[i] * vector[i];
        }
        double norm = Math.sqrt(sum);
        for (int i = 0; i < 128; i++) {
            vector[i] /= norm;
        }
        return vector;
    }

    private String getMockAnalysis(String problemDescription) {
        String desc = problemDescription.toLowerCase();
        String partName = "Weft Selector";
        double x = 0.45, y = 0.55, radius = 0.15;

        if (desc.contains("shaft")) {
            partName = "Main Shaft";
            x = 0.35; y = 0.42; radius = 0.12;
        } else if (desc.contains("needle") || desc.contains("weft")) {
            partName = "Needle";
            x = 0.62; y = 0.38; radius = 0.10;
        } else if (desc.contains("brake") || desc.contains("lever")) {
            partName = "Brake Lever";
            x = 0.28; y = 0.68; radius = 0.14;
        } else if (desc.contains("gear") || desc.contains("box")) {
            partName = "Gear Box";
            x = 0.75; y = 0.58; radius = 0.18;
        }

        return String.format(
                "{\"partName\": \"%s\", \"damageDescription\": \"Observed friction locking and micro-abrasions in active surface area of %s.\", \"x\": %.2f, \"y\": %.2f, \"radius\": %.2f}",
                partName, partName, x, y, radius
        );
    }

    private String getMockReasoning(String visionJson, String context) {
        String partName = "Main Shaft";
        float x = 0.35f, y = 0.42f, radius = 0.12f;

        try {
            JsonNode node = objectMapper.readTree(visionJson);
            partName = node.path("partName").asText("Main Shaft");
            x = (float) node.path("x").asDouble(0.35);
            y = (float) node.path("y").asDouble(0.42);
            radius = (float) node.path("radius").asDouble(0.12);
        } catch (Exception ignored) {}

        String problem;
        String solution;
        String explanation;

        if ("Main Shaft".equalsIgnoreCase(partName)) {
            problem = "Main Shaft Seizure (Lack of Lubrication)";
            solution = "• Power down the loom and apply lock-out tags.\n" +
                       "• Clean cotton lint and residue around the bearing block.\n" +
                       "• Apply 10ml of ISO VG 100 industrial gear oil directly into port B2.\n" +
                       "• Manually rotate the drive wheel to verify free rotation before restarting.";
            explanation = "The main drive shaft has accumulated cotton dust, increasing operational friction and preventing free spin. Proper lubrication will restore nominal operation and prevent motor overload.";
        } else if ("Needle".equalsIgnoreCase(partName)) {
            problem = "Needle Deflection / Alignment Error";
            solution = "• Disconnect loom power.\n" +
                       "• Locate the bent selector needle assembly.\n" +
                       "• Use the alignment key to release the needle lock screw.\n" +
                       "• Fit a new replacement needle and check clearance with the 0.1mm feeler gauge.\n" +
                       "• Tighten lock screw to 4 Nm torque.";
            explanation = "A needle deflection has occurred, causing missed pick cycles. Replacing and calibrating the needle is required to avoid breaking the weft yarn.";
        } else if ("Brake Lever".equalsIgnoreCase(partName)) {
            problem = "Brake Lever Slippage";
            solution = "• Disable loom power supply.\n" +
                       "• Inspect the return spring on the brake arm.\n" +
                       "• Tighten the tension nut clockwise by 2 full rotations.\n" +
                       "• Measure pad thickness (replace if under 2.0mm).\n" +
                       "• Test latch engagements manually.";
            explanation = "Slippage in the brake lever assembly prevents rapid loom stopping during thread breaks, risking loom collision. Adjusting tension restores safety braking force.";
        } else if ("Gear Box".equalsIgnoreCase(partName)) {
            problem = "Gear Box Internal Backlash";
            solution = "• Turn off power and allow machine to cool down.\n" +
                       "• Inspect oil sight glass (level should be at center line).\n" +
                       "• Tighten all external casing flange bolts in a cross pattern.\n" +
                       "• If backlash exceeds 0.5mm, plan replacement of pinion gear during weekly maintenance.";
            explanation = "Internal play in the gearbox results in uneven speed transmission, creating vibrations in the weft selector. Check alignment and oiling immediately.";
        } else {
            problem = "Weft Selector Solenoid Jam";
            solution = "• Shut down loom power.\n" +
                       "• Open the side utility cover to access the selector control panel.\n" +
                       "• Spray electrical contact cleaner on solenoid coils.\n" +
                       "• Reset the selector guide pins manually to ensure spring return operation.";
            explanation = "Solenoid pins are sticking due to build-up of manufacturing particles. Cleaning restore correct yarn color selection cycles.";
        }

        return String.format(
                "{\"problem\": \"%s\", \"solution\": \"%s\", \"explanation\": \"%s\", \"highlightX\": %.2f, \"highlightY\": %.2f, \"highlightRadius\": %.2f}",
                problem, escapeString(solution), escapeString(explanation), x, y, radius
        );
    }

    private String escapeString(String input) {
        return input.replace("\n", "\\n").replace("\"", "\\\"");
    }
}
