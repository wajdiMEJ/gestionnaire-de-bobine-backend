package com.cablemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cablemanager.entity.CommandeFabrication;
import com.cablemanager.entity.StatutCommande;

import java.util.List;

public interface CommandeFabricationRepository extends JpaRepository<CommandeFabrication, Long> {
    List<CommandeFabrication> findByStatut(StatutCommande statut);
}
