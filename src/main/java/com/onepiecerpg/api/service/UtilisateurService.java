package com.onepiecerpg.api.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.onepiecerpg.api.dto.ConnexionRequest;
import com.onepiecerpg.api.dto.ConnexionResponse;
import com.onepiecerpg.api.dto.InscriptionRequest;
import com.onepiecerpg.api.dto.UtilisateurResponseDto;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.repository.UtilisateurRepository;

@Service
public class UtilisateurService {

  private final UtilisateurRepository utilisateurRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final ProgressionJoueurService progressionJoueurService;

  public UtilisateurService(
      UtilisateurRepository utilisateurRepository,
      PasswordEncoder passwordEncoder,
      JwtService jwtService,
      ProgressionJoueurService progressionJoueurService) {
    this.utilisateurRepository = utilisateurRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    this.progressionJoueurService = progressionJoueurService;
  }

  public UtilisateurResponseDto creerUtilisateur(InscriptionRequest request) {
    utilisateurRepository.findByEmail(request.getEmail())
        .ifPresent(utilisateur -> {
          throw new IllegalArgumentException("Cet email existe déjà");
        });

    utilisateurRepository.findByPseudo(request.getPseudo())
        .ifPresent(utilisateur -> {
          throw new IllegalArgumentException("Ce pseudo existe déjà");
        });

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setPseudo(request.getPseudo());
    utilisateur.setEmail(request.getEmail());
    utilisateur.setMotDePasseHash(passwordEncoder.encode(request.getMotDePasse()));
    utilisateur.setRole("USER");

    Utilisateur utilisateurSauvegarde = utilisateurRepository.save(utilisateur);
    progressionJoueurService.creerProgressionInitiale(utilisateurSauvegarde);

    return convertirEnDto(utilisateurSauvegarde);
  }

  public ConnexionResponse connecterUtilisateur(ConnexionRequest request) {
    Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));

    boolean motDePasseValide = passwordEncoder.matches(
        request.getMotDePasse(),
        utilisateur.getMotDePasseHash());

    if (!motDePasseValide) {
      throw new IllegalArgumentException("Email ou mot de passe incorrect");
    }

    String token = jwtService.genererToken(utilisateur);

    return new ConnexionResponse(
        token,
        "Bearer",
        utilisateur.getPseudo(),
        utilisateur.getRole());
  }

  public UtilisateurResponseDto recupererUtilisateurConnecte() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur connecté introuvable"));

    return convertirEnDto(utilisateur);
  }

  private UtilisateurResponseDto convertirEnDto(Utilisateur utilisateur) {
    return new UtilisateurResponseDto(
        utilisateur.getId(),
        utilisateur.getPseudo(),
        utilisateur.getEmail(),
        utilisateur.getRole());
  }
}