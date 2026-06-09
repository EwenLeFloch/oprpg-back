package com.onepiecerpg.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onepiecerpg.api.entity.Combat;
import com.onepiecerpg.api.entity.StatutCombat;

public interface CombatRepository extends JpaRepository<Combat, Long> {
  List<Combat> findByProgressionJoueurId(Long progressionJoueurId);

  Optional<Combat> findByProgressionJoueurIdAndStatut(
      Long progressionJoueurId,
      StatutCombat statut);
}
