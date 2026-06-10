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

import com.onepiecerpg.api.entity.Move;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.TypeMove;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.MoveRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;

class MoveServiceTest {

  private MoveRepository moveRepository;
  private UtilisateurRepository utilisateurRepository;
  private ProgressionJoueurRepository progressionJoueurRepository;
  private MoveService moveService;

  @BeforeEach
  void setUp() {
    moveRepository = mock(MoveRepository.class);
    utilisateurRepository = mock(UtilisateurRepository.class);
    progressionJoueurRepository = mock(ProgressionJoueurRepository.class);

    moveService = new MoveService(
        moveRepository,
        utilisateurRepository,
        progressionJoueurRepository);

    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldGetAllMoves() {
    when(moveRepository.findAll()).thenReturn(List.of(new Move()));

    assertThat(moveService.recupererTousLesMoves()).hasSize(1);
  }

  @Test
  void shouldGetMoveById() {
    Move move = new Move();
    move.setId(1L);
    move.setNom("Coup de poing");

    when(moveRepository.findById(1L)).thenReturn(Optional.of(move));

    assertThat(moveService.recupererMoveParId(1L).getNom()).isEqualTo("Coup de poing");
  }

  @Test
  void shouldGetMoveByName() {
    Move move = new Move();
    move.setId(1L);
    move.setNom("Coup de poing");

    when(moveRepository.findByNom("Coup de poing")).thenReturn(Optional.of(move));

    assertThat(moveService.recupererMoveParNom("Coup de poing").getId()).isEqualTo(1L);
  }

  @Test
  void shouldGetMovesByType() {
    when(moveRepository.findByTypeMove(TypeMove.ATTAQUE)).thenReturn(List.of(new Move()));

    assertThat(moveService.recupererMovesParType(TypeMove.ATTAQUE)).hasSize(1);
  }

  @Test
  void shouldThrowWhenMoveNotFoundById() {
    when(moveRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> moveService.recupererMoveParId(1L))
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("Move introuvable");
  }

  @Test
  void shouldThrowWhenMoveNotFoundByName() {
    when(moveRepository.findByNom("Inconnu")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> moveService.recupererMoveParNom("Inconnu"))
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("Move introuvable");
  }

  @Test
  void shouldGetConnectedCharacterMoves() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("test@test.com", null));

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setId(1L);
    utilisateur.setEmail("test@test.com");

    Move coupDePoing = new Move();
    coupDePoing.setId(2L);
    coupDePoing.setNom("Coup de poing");

    Move coupDePied = new Move();
    coupDePied.setId(1L);
    coupDePied.setNom("Coup de pied");

    Personnage personnage = new Personnage();
    personnage.setId(1L);
    personnage.setNom("Luffy");
    personnage.setMoves(Set.of(coupDePoing, coupDePied));

    ProgressionJoueur progression = new ProgressionJoueur();
    progression.setId(1L);
    progression.setUtilisateur(utilisateur);
    progression.setPersonnage(personnage);

    when(utilisateurRepository.findByEmail("test@test.com")).thenReturn(Optional.of(utilisateur));
    when(progressionJoueurRepository.findByUtilisateur(utilisateur)).thenReturn(Optional.of(progression));

    List<Move> moves = moveService.recupererMovesPersonnageConnecte();

    assertThat(moves)
        .hasSize(2)
        .extracting(Move::getId)
        .containsExactly(1L, 2L);
  }

  @Test
  void shouldThrowWhenConnectedUserNotFound() {
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("missing@test.com", null));

    when(utilisateurRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> moveService.recupererMovesPersonnageConnecte())
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

    assertThatThrownBy(() -> moveService.recupererMovesPersonnageConnecte())
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("Progression du joueur non trouvée");
  }
}