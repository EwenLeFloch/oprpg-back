package com.onepiecerpg.api.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.onepiecerpg.api.entity.Move;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.TypeMove;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.MoveRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;

@Service
public class MoveService {

  private final MoveRepository moveRepository;
  private final UtilisateurRepository utilisateurRepository;
  private final ProgressionJoueurRepository progressionJoueurRepository;

  public MoveService(
      MoveRepository moveRepository,
      UtilisateurRepository utilisateurRepository,
      ProgressionJoueurRepository progressionJoueurRepository) {
    this.moveRepository = moveRepository;
    this.utilisateurRepository = utilisateurRepository;
    this.progressionJoueurRepository = progressionJoueurRepository;
  }

  public List<Move> recupererTousLesMoves() {
    return moveRepository.findAll();
  }

  public Move recupererMoveParId(Long moveId) {
    return moveRepository.findById(moveId)
        .orElseThrow(() -> new RessourceIntrouvableException("Move introuvable"));
  }

  public Move recupererMoveParNom(String nom) {
    return moveRepository.findByNom(nom)
        .orElseThrow(() -> new RessourceIntrouvableException("Move introuvable"));
  }

  public List<Move> recupererMovesParType(TypeMove typeMove) {
    return moveRepository.findByTypeMove(typeMove);
  }

  @Transactional(readOnly = true)
  public List<Move> recupererMovesPersonnageConnecte() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
        .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur non trouvé"));

    ProgressionJoueur progression = progressionJoueurRepository.findByUtilisateur(utilisateur)
        .orElseThrow(() -> new RessourceIntrouvableException("Progression du joueur non trouvée"));

    return progression.getPersonnage().getMoves().stream()
        .sorted(Comparator.comparing(Move::getId))
        .toList();
  }
}