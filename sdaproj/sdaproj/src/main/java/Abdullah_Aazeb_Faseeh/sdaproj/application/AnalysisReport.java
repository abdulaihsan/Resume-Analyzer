package Abdullah_Aazeb_Faseeh.sdaproj.application;

import jakarta.persistence.*;

@Entity
@Table(name = "analysis_results")
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double matchScore; // e.g., 85.5

    @Column(columnDefinition = "TEXT")
    private String feedback; // e.g., "Missing keywords: Java, SQL"

    @ManyToOne
    @JoinColumn(name = "resume_id")
    private Resume resume;

    // Constructors
    public AnalysisReport() {
    }

    public AnalysisReport(double matchScore, String feedback, Resume resume) {
        this.matchScore = matchScore;
        this.feedback = feedback;
        this.resume = resume;
    }

    // Getters...
    public double getMatchScore() {
        return matchScore;
    }

    public String getFeedback() {
        return feedback;
    }
}
