package com.onepiecerpg.api.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.onepiecerpg.api.dto.ConnexionRequest;
import com.onepiecerpg.api.dto.ConnexionResponse;
import com.onepiecerpg.api.dto.InscriptionRequest;
import com.onepiecerpg.api.dto.UtilisateurResponseDto;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.repository.UtilisateurRepository;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

class UtilisateurServiceTest {
  private UtilisateurRepository utilisateurRepository;
  private UtilisateurService utilisateurService;
  private PasswordEncoder passwordEncoder;
  private JwtService jwtService;
  private ProgressionJoueurService progressionJoueurService;

  @BeforeEach
  void setUp() {
    utilisateurRepository = mock(UtilisateurRepository.class);
    passwordEncoder = new BCryptPasswordEncoder();
    jwtService = mock(JwtService.class);
    progressionJoueurService = mock(ProgressionJoueurService.class);

    utilisateurService = new UtilisateurService(
        utilisateurRepository,
        passwordEncoder,
        jwtService,
        progressionJoueurService);
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
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
    when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UtilisateurResponseDto response = utilisateurService.creerUtilisateur(request);

    ArgumentCaptor<Utilisateur> captor = ArgumentCaptor.forClass(Utilisateur.class);

    verify(utilisateurRepository).save(captor.capture());
    verify(progressionJoueurService).creerProgressionInitiale(any(Utilisateur.class));
    Utilisateur utilisateurSauvegarde = captor.getValue();

    assertThat(response.pseudo()).isEqualTo("testuser");
    assertThat(response.email()).isEqualTo("test@test.com");
    assertThat(response.role()).isEqualTo("USER");
    assertThat(passwordEncoder.matches("Password123", utilisateurSauvegarde.getMotDePasseHash())).isTrue();
  }

  @Test
  @DisplayName("Doit refuser la création d'un utilisateur avec un email existant")
  void shouldRejectDuplicateEmail() {
    InscriptionRequest request = new InscriptionRequest();
    request.setPseudo("testuser");
    request.setEmail("test@test.com");
    request.setMotDePasse("Password123");

    Utilisateur utilisateurExistant = new Utilisateur();
    utilisateurExistant.setEmail("test@test.com");

    when(utilisateurRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(utilisateurExistant));

    assertThatThrownBy(() -> utilisateurService.creerUtilisateur(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cet email existe déjà");

    verify(utilisateurRepository, never()).save(any());
  }

  @Test
  @DisplayName("Doit refuser la création d'un utilisateur avec un pseudo existant")
  void shouldRejectDuplicatePseudo() {
    InscriptionRequest request = new InscriptionRequest();
    request.setPseudo("testuser");
    request.setEmail("test@test.com");
    request.setMotDePasse("Password123");

    Utilisateur utilisateurExistant = new Utilisateur();
    utilisateurExistant.setPseudo("testuser");

    when(utilisateurRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
    when(utilisateurRepository.findByPseudo(request.getPseudo())).thenReturn(Optional.of(utilisateurExistant));

    assertThatThrownBy(() -> utilisateurService.creerUtilisateur(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Ce pseudo existe déjà");

    verify(utilisateurRepository, never()).save(any());
  }

  @Test
  @DisplayName("Doit connecter un utilisateur avec des identifiants valides")
  void shouldLoginUser() {
    String motDePasseBrut = "Password123";

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setPseudo("testuser");
    utilisateur.setEmail("test@test.com");
    utilisateur.setMotDePasseHash(passwordEncoder.encode(motDePasseBrut));
    utilisateur.setRole("USER");

    ConnexionRequest request = new ConnexionRequest();
    request.setEmail("test@test.com");
    request.setMotDePasse(motDePasseBrut);

    when(utilisateurRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(utilisateur));
    when(jwtService.genererToken(any(Utilisateur.class))).thenReturn("mocked-token");
    ConnexionResponse response = utilisateurService.connecterUtilisateur(request);

    assertThat(response.token()).isEqualTo("mocked-token");
    assertThat(response.type()).isEqualTo("Bearer");
    assertThat(response.pseudo()).isEqualTo("testuser");
    assertThat(response.role()).isEqualTo("USER");
  }

  @Test
  @DisplayName("Doit refuser une connexion avec un mot de passe incorrect")
  void shouldRejectLoginWithInvalidPassword() {
    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setPseudo("testuser");
    utilisateur.setEmail("luffy@test.com");
    utilisateur.setMotDePasseHash(passwordEncoder.encode("Password123"));
    utilisateur.setRole("USER");

    ConnexionRequest request = new ConnexionRequest();
    request.setEmail("test@test.com");
    request.setMotDePasse("WrongPassword123");

    when(utilisateurRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(utilisateur));

    assertThatThrownBy(() -> utilisateurService.connecterUtilisateur(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email ou mot de passe incorrect");
  }

  @Test
  @DisplayName("Doit refuser une connexion avec un email inconnu")
  void shouldRejectLoginWithUnknownEmail() {
    ConnexionRequest request = new ConnexionRequest();
    request.setEmail("unknown@test.com");
    request.setMotDePasse("Password123");

    when(utilisateurRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> utilisateurService.connecterUtilisateur(request))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email ou mot de passe incorrect");
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
