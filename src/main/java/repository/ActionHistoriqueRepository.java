package repository;

import entity.ActionHistorique;
import entity.TypeAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActionHistoriqueRepository extends JpaRepository<ActionHistorique, Long> {
    List<ActionHistorique> findByType(TypeAction type);
    List<ActionHistorique> findByBobineId(Long bobineId);
    List<ActionHistorique> findByEmployeId(Long employeId);
    List<ActionHistorique> findByOrderByDateDesc();
    List<ActionHistorique> findAllByOrderByDateDesc();
}
