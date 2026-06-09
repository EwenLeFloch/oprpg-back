package com.onepiecerpg.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onepiecerpg.api.entity.Ennemi;

public interface EnnemiRepository extends JpaRepository<Ennemi, Long> {
  List<Ennemi> findByZoneId(Long zoneId);

  List<Ennemi> findByZoneIdAndBossFalse(Long zoneId);

  Optional<Ennemi> findByNom(String nom);
}
