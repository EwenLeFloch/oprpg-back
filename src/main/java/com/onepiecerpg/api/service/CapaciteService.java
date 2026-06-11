package com.onepiecerpg.api.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onepiecerpg.api.entity.Capacite;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.TypeCapacite;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.CapaciteRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;

@Service
public class CapaciteService {

  private final CapaciteRepository capaciteRepository;
  private final UtilisateurRepository utilisateurRepository;
  private final ProgressionJoueurRepository progressionJoueurRepository;

  public CapaciteService(
      CapaciteRepository capaciteRepository,
      UtilisateurRepository utilisateurRepository,
      ProgressionJoueurRepository progressionJoueurRepository) {
    this.capaciteRepository = capaciteRepository;
    this.utilisateurRepository = utilisateurRepository;
    this.progressionJoueurRepository = progressionJoueurRepository;
  }

  public List<Capacite> recupererTousLesCapacites() {
    return capaciteRepository.findAll();
  }

  public Capacite recupererCapaciteParId(Long capaciteId) {
    return capaciteRepository.findById(capaciteId)
        .orElseThrow(() -> new RessourceIntrouvableException("Capacite introuvable"));
  }

  public Capacite recupererCapaciteParNom(String nom) {
    return capaciteRepository.findByNom(nom)
        .orElseThrow(() -> new RessourceIntrouvableException("Capacite introuvable"));
  }

  public List<Capacite> recupererCapacitesParType(TypeCapacite typeCapacite) {
    return capaciteRepository.findByTypeCapacite(typeCapacite);
  }

  @Transactional(readOnly = true)
  public List<Capacite> recupererCapacitesPersonnageConnecte() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
        .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur non trouvé"));

    ProgressionJoueur progression = progressionJoueurRepository.findByUtilisateur(utilisateur)
        .orElseThrow(() -> new RessourceIntrouvableException("Progression du joueur non trouvée"));

    return progression.getPersonnage().getCapacites().stream()
        .sorted(Comparator.comparing(Capacite::getId))
        .toList();
  }
}