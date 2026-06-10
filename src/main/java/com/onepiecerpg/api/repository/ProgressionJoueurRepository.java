package com.onepiecerpg.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.Utilisateur;

public interface ProgressionJoueurRepository extends JpaRepository<ProgressionJoueur, Long> {
  Optional<ProgressionJoueur> findByUtilisateur(Utilisateur utilisateur);

  @EntityGraph(attributePaths = { "personnage", "personnage.moves" })
  Optional<ProgressionJoueur> findWithPersonnageMovesByUtilisateur(Utilisateur utilisateur);
}
