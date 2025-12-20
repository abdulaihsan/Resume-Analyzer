package Abdullah_Aazeb_Faseeh.sdaproj.application;

import Abdullah_Aazeb_Faseeh.sdaproj.persistence.ReportRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.*;

@Service
public class NLPModel {

    private final ReportRepository resumeRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${google.ai.api-key}")
    private String apiKey;

    // UPDATED URL: Using 'gemini-1.5-flash-latest' which is more stable.
    // If this fails, try 'gemini-pro' or 'gemini-2.0-flash'
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=";

    public NLPModel(ReportRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public AnalysisReport analyzeResume(long resumeId, String jobDescription) {
        // 1. Fetch Resume Text
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        String resumeText = (resume.getExtractedText() != null) ? resume.getExtractedText() : "";

        // 2. Call Google Gemini API
        try {
            GeminiResponse analysis = callGeminiAPI(resumeText, jobDescription);

            // 3. Construct Final Feedback String
            String formattedFeedback = formatFeedback(analysis);

            return new AnalysisReport(analysis.score, formattedFeedback, resume);

        } catch (Exception e) {
            e.printStackTrace();
            // Return a friendly error report instead of crashing
            return new AnalysisReport(0, "AI Service Error: " + e.getMessage(), resume);
        }
    }

    private GeminiResponse callGeminiAPI(String resumeText, String jobDesc) throws Exception {
        // A. Construct the Prompt (Asking for JSON)
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

        // B. Build JSON Payload
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);

        Map<String, Object> parts = new HashMap<>();
        parts.put("parts", Collections.singletonList(textPart));

        Map<String, Object> payload = new HashMap<>();
        payload.put("contents", Collections.singletonList(parts));

        // C. Send Request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        String fullUrl = GEMINI_URL + apiKey;

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fullUrl, request, String.class);
            return parseGeminiResponse(response.getBody());
        } catch (org.springframework.web.client.HttpClientErrorException e) {
            // Log the specific Google API error (e.g., 404 or 400)
            throw new Exception("Google API Error: " + e.getResponseBodyAsString());
        }
    }

    private GeminiResponse parseGeminiResponse(String jsonResponse) throws Exception {
        JsonNode root = objectMapper.readTree(jsonResponse);

        // Safety check for empty response
        if (!root.has("candidates") || root.path("candidates").isEmpty()) {
            throw new Exception("AI returned no candidates. It might have blocked the content.");
        }

        String rawText = root.path("candidates").get(0)
                .path("content").path("parts").get(0)
                .path("text").asText();

        // Clean up markdown code blocks if present
        rawText = rawText.replaceAll("```json", "").replaceAll("```", "").trim();

        JsonNode innerJson = objectMapper.readTree(rawText);

        double score = innerJson.path("score").asDouble();
        String feedback = innerJson.path("feedback").asText();

        List<String> missing = new ArrayList<>();
        if (innerJson.has("missing_skills")) {
            innerJson.path("missing_skills").forEach(node -> missing.add(node.asText()));
        }

        return new GeminiResponse(score, feedback, missing);
    }

    private String formatFeedback(GeminiResponse response) {
        StringBuilder sb = new StringBuilder();

        if (response.score > 85)
            sb.append("🚀 EXCELLENT MATCH\n");
        else if (response.score > 60)
            sb.append("✅ GOOD MATCH\n");
        else
            sb.append("⚠️ LOW MATCH\n");

        sb.append(response.feedback).append("\n\n");

        if (!response.missingSkills.isEmpty()) {
            sb.append("MISSING SKILLS:\n");
            for (String skill : response.missingSkills) {
                sb.append("• ").append(skill.toUpperCase()).append("\n");
            }
        }
        return sb.toString();
    }

    private String limitText(String text, int maxLength) {
        if (text == null)
            return "";
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    private static class GeminiResponse {
        double score;
        String feedback;
        List<String> missingSkills;

        public GeminiResponse(double score, String feedback, List<String> missingSkills) {
            this.score = score;
            this.feedback = feedback;
            this.missingSkills = missingSkills;
        }
    }
}