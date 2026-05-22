package com.onepiecerpg.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.onepiecerpg.api.dto.InscriptionRequest;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.repository.UtilisateurRepository;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

class UtilisateurServiceTest {
  private UtilisateurRepository utilisateurRepository;
  private UtilisateurService utilisateurService;
  private PasswordEncoder passwordEncoder;

  @BeforeEach
  void setUp() {
    utilisateurRepository = mock(UtilisateurRepository.class);
    passwordEncoder = new BCryptPasswordEncoder();
    utilisateurService = new UtilisateurService(utilisateurRepository, passwordEncoder);
  }

  @Test
  @DisplayName("Doit créer un utilisateur avec mot de passe hashé ")
  void shouldCreateUser() {
    InscriptionRequest request = new InscriptionRequest();
    request.setPseudo("testuser");
    request.setEmail("test@test.com");
    request.setMotDePasse("Password123");

    when(utilisateurRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
    when(utilisateurRepository.findByPseudo(request.getPseudo())).thenReturn(Optional.empty());

    utilisateurService.creerUtilisateur(request);

    ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);

    verify(utilisateurRepository).save(captor.capture());
    Utilisateur utilisateurSauvegarde = captor.getValue();

    assertThat(utilisateurSauvegarde.getPseudo()).isEqualTo("testuser");
    assertThat(utilisateurSauvegarde.getEmail()).isEqualTo("test@test.com");
    assertThat(passwordEncoder.matches("Password123", utilisateurSauvegarde.getMotDePasseHash())).isTrue();
  }
}
