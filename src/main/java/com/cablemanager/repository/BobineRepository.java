package com.cablemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cablemanager.entity.Bobine;
import com.cablemanager.entity.Couleur;
import com.cablemanager.entity.StatutBobine;

import java.util.List;

public interface BobineRepository extends JpaRepository<Bobine, Long> {
    List<Bobine> findByCouleur(Couleur couleur);
    List<Bobine> findBySection(float section);
    List<Bobine> findByCouleurAndSection(Couleur couleur, float section);
    List<Bobine> findByCouleurAndSectionAndStatut(Couleur couleur, float section, StatutBobine statut);
}
