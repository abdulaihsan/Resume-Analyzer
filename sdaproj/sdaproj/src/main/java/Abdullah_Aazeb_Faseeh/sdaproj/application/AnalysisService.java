package Abdullah_Aazeb_Faseeh.sdaproj.application;

import Abdullah_Aazeb_Faseeh.sdaproj.persistence.JobRepository;
import Abdullah_Aazeb_Faseeh.sdaproj.persistence.ReportRepository;
import Abdullah_Aazeb_Faseeh.sdaproj.persistence.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalysisService {

    private final NLPModel nlpModel;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final ReportRepository reportRepository;

    public AnalysisService(NLPModel nlpModel,
            ResumeRepository resumeRepository,
            JobRepository jobRepository,
            ReportRepository reportRepository) {
        this.nlpModel = nlpModel;
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public AnalysisReport performAnalysis(long jobId, long resumeId) {
        // 1. Fetch Data
        JobDescription job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        // 2. Call AI (Gemini)
        // Note: We use empty string if extractedText is null to prevent crash
        String resumeText = resume.getExtractedText() != null ? resume.getExtractedText() : "";
        NLPModel.AIResult result = nlpModel.analyze(resumeText, job.getContent());

        // 3. Format Output
        String formattedFeedback = formatOutput(result);

        // 4. Save Report
        AnalysisReport report = new AnalysisReport(
                result.score(),
                formattedFeedback,
                resume,
                job);

        return reportRepository.save(report);
    }

    private String formatOutput(NLPModel.AIResult result) {
        StringBuilder sb = new StringBuilder();

        if (result.score() > 85)
            sb.append("🚀 EXCELLENT MATCH\n");
        else if (result.score() > 60)
            sb.append("✅ GOOD MATCH\n");
        else
            sb.append("⚠️ LOW MATCH\n");

        sb.append(result.feedback()).append("\n\n");

        if (!result.missingSkills().isEmpty()) {
            sb.append("MISSING SKILLS:\n");
            for (String skill : result.missingSkills()) {
                sb.append("• ").append(skill.toUpperCase()).append("\n");
            }
        }
        return sb.toString();
    }
}