package Abdullah_Aazeb_Faseeh.sdaproj.controller;

import Abdullah_Aazeb_Faseeh.sdaproj.application.*;
import Abdullah_Aazeb_Faseeh.sdaproj.persistence.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

// DESIGN PATTERN: Controller (MVC)
@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final AnalysisService analysisService;
    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;

    // DESIGN PATTERN: Dependency Injection
    public ResumeController(AnalysisService analysisService,
            ResumeRepository resumeRepository,
            UserRepository userRepository) {
        this.analysisService = analysisService;
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload/{userId}")
    public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file,
            @PathVariable long userId) {
        try {
            User candidate = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!"CANDIDATE".equalsIgnoreCase(candidate.getRole())) {
                return ResponseEntity.status(403).body("Only candidates can upload resumes.");
            }

            // DESIGN PATTERN: Helper Method (Encapsulation)
            String extractedText = extractTextFromPdf(file);

            Resume resume = new Resume(file.getOriginalFilename(), extractedText, candidate);
            resumeRepository.save(resume);

            return ResponseEntity.ok("Resume uploaded successfully! ID: " + resume.getId());

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Error processing PDF: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/apply/{jobId}/{resumeId}")
    public ResponseEntity<?> applyForJob(@PathVariable Long jobId, @PathVariable Long resumeId) {
        try {
            // DESIGN PATTERN: Facade
            AnalysisReport report = analysisService.performAnalysis(jobId, resumeId);

            return ResponseEntity.ok(report);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Application failed: " + e.getMessage());
        }
    }

    private String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setSortByPosition(true);
            return pdfStripper.getText(document);
        }
    }
}