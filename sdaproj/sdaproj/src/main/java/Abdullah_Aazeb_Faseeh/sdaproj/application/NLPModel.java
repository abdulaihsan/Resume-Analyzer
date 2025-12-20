package Abdullah_Aazeb_Faseeh.sdaproj.application;

import Abdullah_Aazeb_Faseeh.sdaproj.persistence.ReportRepository;
import ai.onnxruntime.*;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.*;

@Service
public class NLPModel {

    private final ReportRepository resumeRepository;
    private OrtEnvironment env;
    private OrtSession session;

    private static final int MAX_SEQ_LEN = 512;

    public NLPModel(ReportRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
        try {
            this.env = OrtEnvironment.getEnvironment();
            String modelPath = Paths.get("models", "resume_model.onnx").toString();
            this.session = env.createSession(modelPath, new OrtSession.SessionOptions());

            System.out.println("AI Model loaded successfully from: " + modelPath);
        } catch (OrtException e) {
            System.err.println("Error loading AI Model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public AnalysisReport analyzeResume(long resumeId, String jobDescriptionText) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found"));
        String resumeText = resume.getExtractedText();

        try {
            float[] resumeVector = getEmbedding(resumeText);
            float[] jobVector = getEmbedding(jobDescriptionText);

            double score = calculateCosineSimilarity(resumeVector, jobVector);

            String feedback = generateFeedback(score);

            return new AnalysisReport(score, feedback, resume);

        } catch (OrtException e) {
            throw new RuntimeException("AI Processing Failed: " + e.getMessage());
        }
    }

    private float[] getEmbedding(String text) throws OrtException {
        long[] inputIds = simpleTokenizer(text);
        long[] attentionMask = new long[MAX_SEQ_LEN];
        Arrays.fill(attentionMask, 1);

        long[][] inputIdsBatch = new long[1][MAX_SEQ_LEN];
        long[][] attentionMaskBatch = new long[1][MAX_SEQ_LEN];
        inputIdsBatch[0] = inputIds;
        attentionMaskBatch[0] = attentionMask;

        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put("input_ids", OnnxTensor.createTensor(env, inputIdsBatch));
        inputs.put("attention_mask", OnnxTensor.createTensor(env, attentionMaskBatch));

        try (OrtSession.Result results = session.run(inputs)) {
            float[][] output = (float[][]) results.get(0).getValue();
            return output[0];
        }
    }

    private long[] simpleTokenizer(String text) {
        long[] tokens = new long[MAX_SEQ_LEN];
        String[] words = text.toLowerCase().replaceAll("[^a-z ]", "").split("\\s+");

        tokens[0] = 101;

        for (int i = 0; i < words.length && i < MAX_SEQ_LEN - 2; i++) {
            tokens[i + 1] = Math.abs(words[i].hashCode() % 29000) + 1000;
        }
        int lastIndex = Math.min(words.length + 1, MAX_SEQ_LEN - 1);
        tokens[lastIndex] = 102;

        return tokens;
    }

    private double calculateCosineSimilarity(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        int length = Math.min(vectorA.length, vectorB.length);

        for (int i = 0; i < length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }

        if (normA == 0 || normB == 0)
            return 0.0;

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
        return Math.max(0, similarity * 100);
    }

    private String generateFeedback(double score) {
        if (score > 85)
            return "Excellent Match! Your profile aligns well.";
        if (score > 60)
            return "Good Match. Consider emphasizing key skills.";
        return "Low Match. Review missing keywords and formatting.";
    }
}