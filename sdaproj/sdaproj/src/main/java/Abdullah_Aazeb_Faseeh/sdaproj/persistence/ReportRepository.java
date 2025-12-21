package Abdullah_Aazeb_Faseeh.sdaproj.persistence;

import Abdullah_Aazeb_Faseeh.sdaproj.application.AnalysisReport;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// DESIGN PATTERN: Repository (DAO)
public interface ReportRepository extends JpaRepository<AnalysisReport, Long> {

    // DESIGN PATTERN: Derived Query Method
    // Spring Data JPA automatically generates the query based on the method name
    // logic.
    List<AnalysisReport> findByJobIdOrderByMatchScoreDesc(Long jobId);
}