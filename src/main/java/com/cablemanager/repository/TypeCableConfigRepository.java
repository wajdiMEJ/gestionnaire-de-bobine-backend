package com.cablemanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.cablemanager.entity.TypeCableConfig;

import java.util.Optional;

public interface TypeCableConfigRepository extends JpaRepository<TypeCableConfig, Long> {
    Optional<TypeCableConfig> findByNom(String nom);
}
