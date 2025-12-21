package Abdullah_Aazeb_Faseeh.sdaproj.persistence;

import Abdullah_Aazeb_Faseeh.sdaproj.application.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<AnalysisReport, Long> {
    List<AnalysisReport> findByJobIdOrderByMatchScoreDesc(Long jobId);
}