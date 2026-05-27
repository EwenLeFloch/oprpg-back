package com.onepiecerpg.api.service;

import com.onepiecerpg.api.dto.ConnexionRequest;
import com.onepiecerpg.api.dto.ConnexionResponse;
import com.onepiecerpg.api.dto.InscriptionRequest;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.repository.UtilisateurRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UtilisateurService {
  private final UtilisateurRepository utilisateurRepository;
  private final PasswordEncoder passwordEncoder;

  public UtilisateurService(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder) {
    this.utilisateurRepository = utilisateurRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public Utilisateur creerUtilisateur(InscriptionRequest request) {
    utilisateurRepository.findByEmail(request.getEmail())
        .ifPresent(u -> { throw new IllegalArgumentException("Cet email existe déjà"); });

    utilisateurRepository.findByPseudo(request.getPseudo())
        .ifPresent(u -> { throw new IllegalArgumentException("Ce pseudo existe déjà"); });

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setPseudo(request.getPseudo());
    utilisateur.setEmail(request.getEmail());
    utilisateur.setMotDePasseHash(passwordEncoder.encode(request.getMotDePasse()));
    utilisateur.setRole("USER");

    return utilisateurRepository.save(utilisateur);
  }

  public ConnexionResponse connecterUtilisateur(ConnexionRequest request) {
    Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new IllegalArgumentException("Email ou mot de passe incorrect"));

    boolean motDePasseValide = passwordEncoder.matches(request.getMotDePasse(), utilisateur.getMotDePasseHash());
    if (!motDePasseValide) {
      throw new IllegalArgumentException("Email ou mot de passe incorrect");
    }

    return new ConnexionResponse(
        "Connexion réussie",
        utilisateur.getPseudo(),
        utilisateur.getRole()
    );
  }
}


