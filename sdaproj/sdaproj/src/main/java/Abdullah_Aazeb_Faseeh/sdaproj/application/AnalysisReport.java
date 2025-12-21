package Abdullah_Aazeb_Faseeh.sdaproj.application;

import jakarta.persistence.*;

@Entity
@Table(name = "analysis_reports")
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double matchScore;

    @Column(columnDefinition = "TEXT")
    private String feedback;

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private JobDescription job;

    public AnalysisReport() {
    }

    public AnalysisReport(double matchScore, String feedback, Resume resume, JobDescription job) {
        this.matchScore = matchScore;
        this.feedback = feedback;
        this.resume = resume;
        this.job = job;
    }

    public double getMatchScore() {
        return matchScore;
    }

    public String getFeedback() {
        return feedback;
    }

    public Resume getResume() {
        return resume;
    }

    public JobDescription getJob() {
        return job;
    }
}