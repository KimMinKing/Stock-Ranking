package kmk.bcu.back.repository;


import kmk.bcu.back.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    Optional<AccessToken> findTopByOrderByIdDesc();
}
