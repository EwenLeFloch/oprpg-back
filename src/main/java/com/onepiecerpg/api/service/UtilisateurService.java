package com.onepiecerpg.api.service;

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

  public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
    utilisateurRepository.findByMail(utilisateur.getMail())
        .ifPresent(u -> { throw new IllegalArgumentException("L'email existe déjà"); });

    utilisateurRepository.findByUsername(utilisateur.getUsername())
        .ifPresent(u -> { throw new IllegalArgumentException("Le nom d'utilisateur existe déjà"); });

    utilisateur.setPassword(passwordEncoder.encode(utilisateur.getPassword()));
    utilisateur.setRole("USER");

    return utilisateurRepository.save(utilisateur);
  }
}
