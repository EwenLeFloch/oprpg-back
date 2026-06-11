package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.onepiecerpg.api.entity.Capacite;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.TypeCapacite;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.CapaciteRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;

class CapaciteServiceTest {

  private CapaciteRepository capaciteRepository;
  private UtilisateurRepository utilisateurRepository;
  private ProgressionJoueurRepository progressionJoueurRepository;
  private CapaciteService capaciteService;

  @BeforeEach
  void setUp() {
    capaciteRepository = mock(CapaciteRepository.class);
    utilisateurRepository = mock(UtilisateurRepository.class);
    progressionJoueurRepository = mock(ProgressionJoueurRepository.class);

    capaciteService = new CapaciteService(
        capaciteRepository,
        utilisateurRepository,
        progressionJoueurRepository);

    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldGetAllCapacites() {
    when(capaciteRepository.findAll()).thenReturn(List.of(new Capacite()));

    assertThat(capaciteService.recupererTousLesCapacites()).hasSize(1);
  }

  @Test
  void shouldGetCapaciteById() {
    Capacite capacite = new Capacite();
    capacite.setId(1L);
    capacite.setNom("Coup de poing");

    when(capaciteRepository.findById(1L)).thenReturn(Optional.of(capacite));

    assertThat(capaciteService.recupererCapaciteParId(1L).getNom()).isEqualTo("Coup de poing");
  }

  @Test
  void shouldGetCapaciteByName() {
    Capacite capacite = new Capacite();
    capacite.setId(1L);
    capacite.setNom("Coup de poing");

    when(capaciteRepository.findByNom("Coup de poing")).thenReturn(Optional.of(capacite));

    assertThat(capaciteService.recupererCapaciteParNom("Coup de poing").getId()).isEqualTo(1L);
  }

  @Test
  void shouldGetCapacitesByType() {
    when(capaciteRepository.findByTypeCapacite(TypeCapacite.ATTAQUE)).thenReturn(List.of(new Capacite()));

    assertThat(capaciteService.recupererCapacitesParType(TypeCapacite.ATTAQUE)).hasSize(1);
  }

  @Test
  void shouldThrowWhenCapaciteNotFoundById() {
    when(capaciteRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> capaciteService.recupererCapaciteParId(1L))
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("Capacite introuvable");
  }

  @Test
  void shouldThrowWhenCapaciteNotFoundByName() {
    when(capaciteRepository.findByNom("Inconnu")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> capaciteService.recupererCapaciteParNom("Inconnu"))
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("Capacite introuvable");
  }

  @Test
  void shouldGetConnectedCharacterCapacites() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("test@test.com", null));

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setId(1L);
    utilisateur.setEmail("test@test.com");

    Capacite coupDePoing = new Capacite();
    coupDePoing.setId(2L);
    coupDePoing.setNom("Coup de poing");

    Capacite coupDePied = new Capacite();
    coupDePied.setId(1L);
    coupDePied.setNom("Coup de pied");

    Personnage personnage = new Personnage();
    personnage.setId(1L);
    personnage.setNom("Luffy");
    personnage.setCapacites(Set.of(coupDePoing, coupDePied));

    ProgressionJoueur progression = new ProgressionJoueur();
    progression.setId(1L);
    progression.setUtilisateur(utilisateur);
    progression.setPersonnage(personnage);

    when(utilisateurRepository.findByEmail("test@test.com")).thenReturn(Optional.of(utilisateur));
    when(progressionJoueurRepository.findByUtilisateur(utilisateur)).thenReturn(Optional.of(progression));

    List<Capacite> capacites = capaciteService.recupererCapacitesPersonnageConnecte();

    assertThat(capacites)
        .hasSize(2)
        .extracting(Capacite::getId)
        .containsExactly(1L, 2L);
  }

  @Test
  void shouldThrowWhenConnectedUserNotFound() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("missing@test.com", null));

    when(utilisateurRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> capaciteService.recupererCapacitesPersonnageConnecte())
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("Utilisateur non trouvé");
  }

  @Test
  void shouldThrowWhenConnectedProgressionNotFound() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("test@test.com", null));

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setId(1L);
    utilisateur.setEmail("test@test.com");

    when(utilisateurRepository.findByEmail("test@test.com")).thenReturn(Optional.of(utilisateur));
    when(progressionJoueurRepository.findByUtilisateur(utilisateur)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> capaciteService.recupererCapacitesPersonnageConnecte())
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("Progression du joueur non trouvée");
  }
}