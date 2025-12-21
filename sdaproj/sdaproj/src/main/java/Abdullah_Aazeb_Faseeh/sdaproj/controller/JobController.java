package Abdullah_Aazeb_Faseeh.sdaproj.controller;

import Abdullah_Aazeb_Faseeh.sdaproj.application.*;
import Abdullah_Aazeb_Faseeh.sdaproj.persistence.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// DESIGN PATTERN: Controller (MVC)
@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    // DESIGN PATTERN: Dependency Injection
    public JobController(JobRepository jobRepository, UserRepository userRepository,
            ReportRepository reportRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
    }

    @PostMapping("/post/{recruiterId}")
    public ResponseEntity<?> postJob(@PathVariable long recruiterId, @RequestBody JobDescription job) {
        User recruiter = userRepository.findById(recruiterId)
                .orElseThrow(() -> new RuntimeException("Recruiter not found"));

        if (!"RECRUITER".equalsIgnoreCase(recruiter.getRole())) {
            return ResponseEntity.status(403).body("Only recruiters can post jobs");
        }

        JobDescription newJob = new JobDescription(job.getTitle(), job.getContent(), recruiter);
        jobRepository.save(newJob);
        return ResponseEntity.ok(newJob);
    }

    @GetMapping("/all")
    public List<JobDescription> getAllJobs() {
        return jobRepository.findAll();
    }

    @GetMapping("/{jobId}/candidates")
    public ResponseEntity<?> getJobCandidates(@PathVariable long jobId) {
        List<AnalysisReport> applications = reportRepository.findByJobIdOrderByMatchScoreDesc(jobId);
        return ResponseEntity.ok(applications);
    }
}