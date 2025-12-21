package Abdullah_Aazeb_Faseeh.sdaproj.application;

import Abdullah_Aazeb_Faseeh.sdaproj.persistence.JobRepository;
import Abdullah_Aazeb_Faseeh.sdaproj.persistence.ReportRepository;
import Abdullah_Aazeb_Faseeh.sdaproj.persistence.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// DESIGN PATTERN: Service Layer
// Encapsulates business logic, coordinating between repositories and external services (NLP).
@Service
public class AnalysisService {

    private final NLPModel nlpModel;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final ReportRepository reportRepository;

    // DESIGN PATTERN: Facade
    // Provides a simplified interface (performAnalysis) to a complex subsystem (AI
    // analysis + DB operations).
    public AnalysisService(NLPModel nlpModel,
            ResumeRepository resumeRepository,
            JobRepository jobRepository,
            ReportRepository reportRepository) {
        this.nlpModel = nlpModel;
        this.resumeRepository = resumeRepository;
        this.jobRepository = jobRepository;
        this.reportRepository = reportRepository;
    }

    // DESIGN PATTERN: Proxy (Transaction Management)
    // Spring uses AOP proxies to manage the transaction boundary around this
    // method.
    @Transactional
    public AnalysisReport performAnalysis(long jobId, long resumeId) {
        JobDescription job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found: " + jobId));
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        String resumeText = resume.getExtractedText() != null ? resume.getExtractedText() : "";
        NLPModel.AIResult result = nlpModel.analyze(resumeText, job.getContent());

        String formattedFeedback = formatOutput(result);

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