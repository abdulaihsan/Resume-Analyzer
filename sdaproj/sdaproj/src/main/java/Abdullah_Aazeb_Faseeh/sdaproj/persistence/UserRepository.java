package Abdullah_Aazeb_Faseeh.sdaproj.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import Abdullah_Aazeb_Faseeh.sdaproj.application.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
