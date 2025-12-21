package Abdullah_Aazeb_Faseeh.sdaproj.application;

import jakarta.persistence.*;
import java.time.LocalDateTime;

// DESIGN PATTERN: Entity (Domain Model)
@Entity
@Table(name = "job_descriptions")
public class JobDescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;
    private LocalDateTime postedAt;

    // DESIGN PATTERN: Association (Many-to-One)
    @ManyToOne
    @JoinColumn(name = "recruiter_id")
    private User recruiter;

    public JobDescription() {
    }

    public JobDescription(String title, String content, User recruiter) {
        this.title = title;
        this.content = content;
        this.recruiter = recruiter;
        this.postedAt = LocalDateTime.now();
    }

    // DESIGN PATTERN: Encapsulation
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public User getRecruiter() {
        return recruiter;
    }

    public LocalDateTime getPostedAt() {
        return postedAt;
    }

}