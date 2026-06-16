package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.onepiecerpg.api.dto.ConnexionRequest;
import com.onepiecerpg.api.dto.ConnexionResponse;
import com.onepiecerpg.api.dto.UtilisateurResponseDto;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.repository.UtilisateurRepository;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

  @Mock
  private UtilisateurRepository utilisateurRepository;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private JwtService jwtService;

  @Mock
  private ProgressionJoueurService progressionJoueurService;

  @InjectMocks
  private UtilisateurService utilisateurService;

  @Test
  @DisplayName("Doit connecter un utilisateur avec son email")
  void shouldLoginUserWithEmail() {
    String motDePasseBrut = "Password123";

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setPseudo("testuser");
    utilisateur.setEmail("test@test.com");
    utilisateur.setMotDePasseHash(passwordEncoder.encode(motDePasseBrut));
    utilisateur.setRole("USER");

    ConnexionRequest request = new ConnexionRequest();
    request.setIdentifiant("test@test.com");
    request.setMotDePasse(motDePasseBrut);

    when(utilisateurRepository.findByEmail(request.getIdentifiant())).thenReturn(Optional.of(utilisateur));
    when(passwordEncoder.matches(any(), any())).thenReturn(true);
    when(jwtService.genererToken(any(Utilisateur.class))).thenReturn("mocked-token");

    ConnexionResponse response = utilisateurService.connecterUtilisateur(request);

    assertThat(response.token()).isEqualTo("mocked-token");
    assertThat(response.type()).isEqualTo("Bearer");
    assertThat(response.pseudo()).isEqualTo("testuser");
    assertThat(response.role()).isEqualTo("USER");
  }

  @Test
  @DisplayName("Doit connecter un utilisateur avec son pseudo")
  void shouldLoginUserWithPseudo() {
    String motDePasseBrut = "Password123";

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setPseudo("testuser");
    utilisateur.setEmail("test@test.com");
    utilisateur.setMotDePasseHash(motDePasseBrut);
    utilisateur.setRole("USER");

    ConnexionRequest request = new ConnexionRequest();
    request.setIdentifiant("testuser");
    request.setMotDePasse(motDePasseBrut);

    when(utilisateurRepository.findByEmail(request.getIdentifiant())).thenReturn(Optional.empty());
    when(utilisateurRepository.findByPseudo(request.getIdentifiant())).thenReturn(Optional.of(utilisateur));
    when(passwordEncoder.matches(any(), any())).thenReturn(true);
    when(jwtService.genererToken(any(Utilisateur.class))).thenReturn("mocked-token");

    ConnexionResponse response = utilisateurService.connecterUtilisateur(request);

    assertThat(response.token()).isEqualTo("mocked-token");
    assertThat(response.pseudo()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("Doit refuser une connexion avec un mot de passe incorrect")
  void shouldRejectLoginWithInvalidPassword() {
    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setPseudo("testuser");
    utilisateur.setEmail("test@test.com");
    utilisateur.setMotDePasseHash("hashed");
    utilisateur.setRole("USER");

    ConnexionRequest request = new ConnexionRequest();
    request.setIdentifiant("test@test.com");
    request.setMotDePasse("WrongPassword123");

    when(utilisateurRepository.findByEmail(request.getIdentifiant())).thenReturn(Optional.of(utilisateur));
    when(passwordEncoder.matches(any(), any())).thenReturn(false);

    assertThatThrownBy(() -> utilisateurService.connecterUtilisateur(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Identifiant ou mot de passe incorrect");
  }

  @Test
  @DisplayName("Doit refuser une connexion avec un identifiant inconnu")
  void shouldRejectLoginWithUnknownIdentifiant() {
    ConnexionRequest request = new ConnexionRequest();
    request.setIdentifiant("inconnu@test.com");
    request.setMotDePasse("Password123");

    when(utilisateurRepository.findByEmail(request.getIdentifiant())).thenReturn(Optional.empty());
    when(utilisateurRepository.findByPseudo(request.getIdentifiant())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> utilisateurService.connecterUtilisateur(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Identifiant ou mot de passe incorrect");
  }

  @Test
  @DisplayName("Doit récupérer les informations de l'utilisateur connecté")
  void shouldGetConnectedUser() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("test@test.com", null));

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setId(1L);
    utilisateur.setPseudo("testuser");
    utilisateur.setEmail("test@test.com");
    utilisateur.setRole("USER");

    when(utilisateurRepository.findByEmail("test@test.com")).thenReturn(Optional.of(utilisateur));

    UtilisateurResponseDto response = utilisateurService.recupererUtilisateurConnecte();

    assertThat(response.id()).isEqualTo(1L);
    assertThat(response.pseudo()).isEqualTo("testuser");
    assertThat(response.email()).isEqualTo("test@test.com");
    assertThat(response.role()).isEqualTo("USER");
  }

  @Test
  @DisplayName("Doit refuser la récupération de l'utilisateur connecté si l'email n'est pas trouvé")
  void shouldRejectGetConnectedUserWithUnknownEmail() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("unknown@test.com", null));

    when(utilisateurRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> utilisateurService.recupererUtilisateurConnecte())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Utilisateur connecté introuvable");
  }
}