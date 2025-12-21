package Abdullah_Aazeb_Faseeh.sdaproj.application;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

// DESIGN PATTERN: Entity (Domain Model)
@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Column(columnDefinition = "TEXT")
    @JsonIgnore
    private String extractedText;

    // DESIGN PATTERN: Association
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

    // DESIGN PATTERN: Encapsulation
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