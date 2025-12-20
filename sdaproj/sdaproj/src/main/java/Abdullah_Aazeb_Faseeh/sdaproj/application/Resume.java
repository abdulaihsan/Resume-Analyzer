package Abdullah_Aazeb_Faseeh.sdaproj.application;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private String filePath; // Where the PDF is stored on disk

    @Column(columnDefinition = "TEXT") // Allows storing large amounts of text
    private String extractedText;

    private LocalDateTime uploadDate;

    // Many Resumes belong to One User
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Constructors
    public Resume() {
    }

    public Resume(String fileName, String filePath, String extractedText, User user) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.extractedText = extractedText;
        this.user = user;
        this.uploadDate = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}