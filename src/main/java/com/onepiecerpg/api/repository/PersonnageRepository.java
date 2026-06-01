package com.onepiecerpg.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.onepiecerpg.api.entity.Personnage;

public interface PersonnageRepository extends JpaRepository<Personnage, Long> {
    Optional<Personnage> findByNom(String nom);
}
