package com.onepiecerpg.api.service;

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
        .ifPresent(u -> { throw new IllegalArgumentException("L'email existe déjà"); });

    utilisateurRepository.findByPseudo(request.getPseudo())
        .ifPresent(u -> { throw new IllegalArgumentException("Le pseudo existe déjà"); });

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setPseudo(request.getPseudo());
    utilisateur.setEmail(request.getEmail());
    utilisateur.setMotDePasseHash(passwordEncoder.encode(request.getMotDePasse()));
    utilisateur.setRole("USER");

    return utilisateurRepository.save(utilisateur);
  }
}
