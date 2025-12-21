package Abdullah_Aazeb_Faseeh.sdaproj.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import Abdullah_Aazeb_Faseeh.sdaproj.application.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
