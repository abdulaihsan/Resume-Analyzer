package Abdullah_Aazeb_Faseeh.sdaproj.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.*;

// DESIGN PATTERN: Adapter / Gateway
@Service
public class NLPModel {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.ai.api-key}")
    private String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=";

    public NLPModel() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // DESIGN PATTERN: Data Transfer Object (DTO)
    public record AIResult(double score, String feedback, List<String> missingSkills) {
    }

    public AIResult analyze(String resumeText, String jobDescription) {
        try {
            return callGeminiAPI(resumeText, jobDescription);
        } catch (Exception e) {
            e.printStackTrace();
            return new AIResult(0.0, "AI Connection Failed: " + e.getMessage(), Collections.emptyList());
        }
    }

    private AIResult callGeminiAPI(String resumeText, String jobDesc) throws Exception {
        String prompt = String.format(
                "Act as an expert Technical Recruiter. Compare this Resume against the Job Description.\n\n" +
                        "RESUME:\n%s\n\n" +
                        "JOB DESCRIPTION:\n%s\n\n" +
                        "OUTPUT INSTRUCTIONS:\n" +
                        "Return ONLY a raw JSON object (no markdown) with these 3 fields:\n" +
                        "1. 'score': A number 0-100.\n" +
                        "2. 'feedback': A 2-sentence summary.\n" +
                        "3. 'missing_skills': A list of strings (max 5) of missing skills.\n",
                limitText(resumeText, 8000),
                limitText(jobDesc, 2000));

        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", Collections.singletonList(textPart));

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", Collections.singletonList(parts));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        String fullUrl = GEMINI_URL + apiKey;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, request, String.class);
            return parseGeminiResponse(response.getBody());
        } catch (HttpClientErrorException e) {
            throw new Exception("Google API Error: " + e.getResponseBodyAsString());
        }
    }

    private AIResult parseGeminiResponse(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);

        if (!root.has("candidates") || root.path("candidates").isEmpty()) {
            throw new Exception("Blocked by Safety Filters or No Response.");
        }

        String rawText = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        rawText = rawText.replaceAll("```json", "").replaceAll("```", "").trim();

        JsonNode innerJson = objectMapper.readTree(rawText);

        double score = innerJson.path("score").asDouble();
        String feedback = innerJson.path("feedback").asText();

        List<String> missing = new ArrayList<>();
        if (innerJson.has("missing_skills")) {
            innerJson.path("missing_skills").forEach(node -> missing.add(node.asText()));
        }

        return new AIResult(score, feedback, missing);
    }

    private String limitText(String text, int maxLength) {
        if (text == null)
            return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }
}