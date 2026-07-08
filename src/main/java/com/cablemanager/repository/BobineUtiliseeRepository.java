package com.cablemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cablemanager.entity.BobineUtilisee;

import java.util.List;

public interface BobineUtiliseeRepository extends JpaRepository<BobineUtilisee, Long> {
    List<BobineUtilisee> findByBobineId(Long bobineId);
}
