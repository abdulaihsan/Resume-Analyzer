package Abdullah_Aazeb_Faseeh.sdaproj.controller;

import Abdullah_Aazeb_Faseeh.sdaproj.application.AnalysisReport;
import Abdullah_Aazeb_Faseeh.sdaproj.application.AnalysisService;
import Abdullah_Aazeb_Faseeh.sdaproj.application.NLPModel;
import Abdullah_Aazeb_Faseeh.sdaproj.application.Resume;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    private final AnalysisService resumeService;
    private final NLPModel aiService; // Inject the new AI Brain

    // Constructor Injection for both Services
    public ResumeController(AnalysisService resumeService, NLPModel aiService) {
        this.resumeService = resumeService;
        this.aiService = aiService;
    }

    @PostMapping("/upload/{userId}")
    public ResponseEntity<String> uploadResume(@RequestParam("file") MultipartFile file,
            @PathVariable Long userId) {
        try {
            Resume savedResume = resumeService.storeResume(file, userId);
            // We return a simple string so the frontend can easily verify success
            return ResponseEntity.ok("Resume uploaded successfully! ID: " + savedResume.getId());
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error processing file: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/analyze/{resumeId}")
    public ResponseEntity<String> analyzeResume(@PathVariable Long resumeId,
            @RequestBody String jobDescription) {
        try {
            AnalysisReport result = aiService.analyzeResume(resumeId, jobDescription);

            // Format the output to look like a system log
            String response = String.format(
                    "SYSTEM ANALYSIS REPORT\n" +
                            "----------------------\n" +
                            "COMPATIBILITY SCORE : %.2f%%\n" +
                            "AI FEEDBACK         : %s",
                    result.getMatchScore(),
                    result.getFeedback());

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("ERROR: " + e.getMessage());
        }
    }
}