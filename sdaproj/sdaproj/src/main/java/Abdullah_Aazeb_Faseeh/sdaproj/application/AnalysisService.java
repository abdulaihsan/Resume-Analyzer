package Abdullah_Aazeb_Faseeh.sdaproj.application;

import Abdullah_Aazeb_Faseeh.sdaproj.persistence.ReportRepository;
import Abdullah_Aazeb_Faseeh.sdaproj.persistence.UserRepository;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AnalysisService {

    private final ReportRepository resumeRepository;
    private final UserRepository userRepository;

    // Directory where PDF files will be saved locally
    private final String UPLOAD_DIR = "uploads/";

    public AnalysisService(ReportRepository resumeRepository, UserRepository userRepository) {
        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
    }

    public Resume storeResume(MultipartFile file, long userId) throws IOException {
        // 1. Ensure the upload directory exists
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // 2. Save the file to the local disk
        String fileName = file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + System.currentTimeMillis() + "_" + fileName);
        Files.write(filePath, file.getBytes());

        // 3. Extract Text using Apache PDFBox
        String extractedText = "";
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFTextStripper stripper = new PDFTextStripper();
            extractedText = stripper.getText(document);
        }
        if (extractedText != null) {
            extractedText = extractedText.replace("\u0000", "");
        }
        // 4. Find the User (For this MVP, we assume user exists)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 5. Create and Save the Resume Entity
        Resume resume = new Resume(fileName, filePath.toString(), extractedText, user);
        return resumeRepository.save(resume);
    }
}