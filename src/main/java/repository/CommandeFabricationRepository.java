package repository;

import entity.CommandeFabrication;
import entity.StatutCommande;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommandeFabricationRepository extends JpaRepository<CommandeFabrication, Long> {
    List<CommandeFabrication> findByStatut(StatutCommande statut);
}
