package com.onepiecerpg.api.repository;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.onepiecerpg.api.entity.Utilisateur;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UtilisateurRepositoryTest {
  @Autowired
  private UtilisateurRepository utilisateurRepository;

  @Test
  @DisplayName("Doit trouver un utilisateur par email")
  void shouldFindUserByEmail() {
    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setPseudo("testuser");
    utilisateur.setEmail("test@test.com");
    utilisateur.setMotDePasseHash("hashedpassword");
    utilisateur.setRole("USER");
    utilisateurRepository.save(utilisateur);

    Optional<Utilisateur> result = utilisateurRepository.findByEmail("test@test.com");
    assertThat(result).isPresent();
    assertThat(result.get().getPseudo()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("Doit trouver un utilisateur par pseudo")
  void shouldFindUserByPseudo() {
    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setPseudo("testuser");
    utilisateur.setEmail("test@test.com");
    utilisateur.setMotDePasseHash("hashedpassword");
    utilisateur.setRole("USER");
    utilisateurRepository.save(utilisateur);

    Optional<Utilisateur> result = utilisateurRepository.findByPseudo("testuser");
    assertThat(result).isPresent();
    assertThat(result.get().getEmail()).isEqualTo("test@test.com");
  }

  @Test
  @DisplayName("Doit retourner vide si l'email n'existe pas")
  void shouldReturnEmptyWhenEmailNotFound() {
    Optional<Utilisateur> result = utilisateurRepository.findByEmail("email-invalide@test.com");
    assertThat(result).isEmpty();
  }
}
