package repository;

import entity.Bobine;
import entity.Couleur;
import entity.StatutBobine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BobineRepository extends JpaRepository<Bobine, Long> {
    List<Bobine> findByCouleur(Couleur couleur);
    List<Bobine> findBySection(float section);
    List<Bobine> findByCouleurAndSection(Couleur couleur, float section);
    List<Bobine> findByCouleurAndSectionAndStatut(Couleur couleur, float section, StatutBobine statut);
}
