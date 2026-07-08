package com.cablemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cablemanager.entity.Employe;

import java.util.Optional;

public interface EmployeRepository extends JpaRepository<Employe, Long> {
    Optional<Employe> findByMatricule(String matricule);
}
