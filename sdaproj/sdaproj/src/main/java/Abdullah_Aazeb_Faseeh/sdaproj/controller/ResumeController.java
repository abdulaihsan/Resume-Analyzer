package Abdullah_Aazeb_Faseeh.sdaproj.controller;

import Abdullah_Aazeb_Faseeh.sdaproj.application.*;
import Abdullah_Aazeb_Faseeh.sdaproj.persistence.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final AnalysisService analysisService; // New Service
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    // Constructor Injection
    public ResumeController(AnalysisService analysisService,
            ResumeRepository resumeRepository,
            UserRepository userRepository) {
        this.analysisService = analysisService;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    // 1. UPLOAD RESUME
    @PostMapping("/upload/{userId}")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
            @PathVariable long userId) {
        try {
            // Verify User exists and is a Candidate
            User candidate = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!"CANDIDATE".equalsIgnoreCase(candidate.getRole())) {
                return ResponseEntity.status(403).body("Only candidates can upload resumes.");
            }

            // Extract Text from PDF
            String extractedText = extractTextFromPdf(file);

            // Create and Save Resume
            Resume resume = new Resume(file.getOriginalFilename(), extractedText, candidate);
            resumeRepository.save(resume);

            return ResponseEntity.ok("Resume uploaded successfully! ID: " + resume.getId());

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error processing PDF: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }

    // 2. APPLY FOR JOB (Updated to use AnalysisService)
    @PostMapping("/apply/{jobId}/{resumeId}")
    public ResponseEntity<?> applyForJob(@PathVariable Long jobId, @PathVariable Long resumeId) {
        try {
            // The AnalysisService now handles fetching entities, calling AI, formatting,
            // and saving.
            AnalysisReport report = analysisService.performAnalysis(jobId, resumeId);

            return ResponseEntity.ok(report);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Application failed: " + e.getMessage());
        }
    }

    // Helper Method
    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }
}