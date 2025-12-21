package Abdullah_Aazeb_Faseeh.sdaproj.application;

import jakarta.persistence.*;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String extractedText;

    @ManyToOne
    @JoinColumn(name = "candidate_id")
    private User candidate;

    public Resume() {
    }

    public Resume(String fileName, String extractedText, User candidate) {
        this.fileName = fileName;
        this.extractedText = extractedText;
        this.candidate = candidate;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public User getCandidate() {
        return candidate;
    }

    public void setCandidate(User user) {
        this.candidate = user;
    }
}