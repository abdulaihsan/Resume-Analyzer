package Abdullah_Aazeb_Faseeh.sdaproj.persistence;

import Abdullah_Aazeb_Faseeh.sdaproj.application.JobDescription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// DESIGN PATTERN: Repository (DAO)
public interface JobRepository extends JpaRepository<JobDescription, Long> {
    List<JobDescription> findByRecruiterId(Long recruiterId);
}