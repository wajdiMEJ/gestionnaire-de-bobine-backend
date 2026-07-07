package repository;

import entity.TypeCableConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TypeCableConfigRepository extends JpaRepository<TypeCableConfig, Long> {
    Optional<TypeCableConfig> findByNom(String nom);
}
