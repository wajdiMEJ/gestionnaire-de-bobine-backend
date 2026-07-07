package repository;

import entity.BobineUtilisee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BobineUtiliseeRepository extends JpaRepository<BobineUtilisee, Long> {
    List<BobineUtilisee> findByBobineId(Long bobineId);
}
