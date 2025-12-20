package Abdullah_Aazeb_Faseeh.sdaproj.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import Abdullah_Aazeb_Faseeh.sdaproj.application.Resume;

public interface ReportRepository extends JpaRepository<Resume, Long> {
}