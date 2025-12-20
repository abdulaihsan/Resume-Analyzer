package Abdullah_Aazeb_Faseeh.sdaproj.application;

import Abdullah_Aazeb_Faseeh.sdaproj.persistence.ReportRepository;

import org.apache.commons.text.similarity.CosineSimilarity;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class NLPModel {

    private final ReportRepository resumeRepository;

    public NLPModel(ReportRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    public AnalysisReport analyzeResume(long resumeId, String jobDescriptionText) {
        // 1. Fetch the Resume
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));

        String resumeText = resume.getExtractedText();

        // 2. Calculate Similarity Score (0 to 100%)
        double score = calculateCosineSimilarity(resumeText, jobDescriptionText);

        // 3. Generate Simple Feedback (Optional: Improve this later)
        String feedback = "Match Score calculated based on keyword frequency.";

        // 4. Return the result object (You can save this to DB if you want)
        return new AnalysisReport(score, feedback, resume);
    }

    // --- The Math Logic ---
    private double calculateCosineSimilarity(String text1, String text2) {
        // Convert text to word frequency maps (Vectors)
        Map<CharSequence, Integer> vector1 = getWordFrequency(text1);
        Map<CharSequence, Integer> vector2 = getWordFrequency(text2);

        // Use Apache Commons CosineSimilarity
        CosineSimilarity cosine = new CosineSimilarity();
        Double result = cosine.cosineSimilarity(vector1, vector2);

        // Convert 0.0-1.0 to 0-100%
        return (result != null) ? result * 100 : 0.0;
    }

    private Map<CharSequence, Integer> getWordFrequency(String text) {
        // Clean text: lowercase, remove non-letters
        String cleanText = text.toLowerCase().replaceAll("[^a-z ]", "");

        // Split by space and count occurrences
        return Arrays.stream(cleanText.split("\\s+"))
                .filter(word -> word.length() > 2) // Ignore tiny words like "is", "at"
                .collect(Collectors.toMap(w -> w, w -> 1, (a, b) -> a + b));
    }
}