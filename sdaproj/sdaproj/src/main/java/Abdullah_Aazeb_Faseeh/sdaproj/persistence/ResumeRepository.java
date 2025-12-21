package Abdullah_Aazeb_Faseeh.sdaproj.persistence;

import Abdullah_Aazeb_Faseeh.sdaproj.application.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// DESIGN PATTERN: Repository (DAO)
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByCandidateId(Long candidateId);
}