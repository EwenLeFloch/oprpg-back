package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.onepiecerpg.api.dto.CombatResponse;
import com.onepiecerpg.api.entity.Combat;
import com.onepiecerpg.api.entity.Ennemi;
import com.onepiecerpg.api.entity.Capacite;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.StatutCombat;
import com.onepiecerpg.api.entity.TypeCapacite;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.repository.CombatRepository;
import com.onepiecerpg.api.repository.EnnemiRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;

class CombatServiceTest {

  private CombatRepository combatRepository;
  private EnnemiRepository ennemiRepository;
  private ProgressionJoueurRepository progressionJoueurRepository;
  private UtilisateurRepository utilisateurRepository;
  private CombatService combatService;

  @BeforeEach
  void setUp() {
    combatRepository = mock(CombatRepository.class);
    ennemiRepository = mock(EnnemiRepository.class);
    progressionJoueurRepository = mock(ProgressionJoueurRepository.class);
    utilisateurRepository = mock(UtilisateurRepository.class);

    combatService = new CombatService(
        combatRepository,
        ennemiRepository,
        progressionJoueurRepository,
        utilisateurRepository);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("test@test.com", null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldStartCombat() {
    ProgressionJoueur progression = progression();
    Ennemi ennemi = ennemi("Bandit", 20, 3, 5, 5);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());
    when(ennemiRepository.findById(1L)).thenReturn(Optional.of(ennemi));
    when(combatRepository.save(any(Combat.class))).thenAnswer(invocation -> {
      Combat combat = invocation.getArgument(0);
      combat.setId(1L);
      return combat;
    });

    CombatResponse response = combatService.demarrerCombat(1L);

    assertThat(response.combatId()).isEqualTo(1L);
    assertThat(response.ennemi()).isEqualTo("Bandit");
    assertThat(response.vieEnnemiActuelle()).isEqualTo(20);
    assertThat(response.vieJoueurActuelle()).isEqualTo(30);
    assertThat(response.statut()).isEqualTo(StatutCombat.EN_COURS);
    assertThat(response.recompense()).isNull();
  }

  @Test
  void shouldRejectStartCombatWhenAlreadyInCombat() {
    ProgressionJoueur progression = progression();

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(new Combat()));

    assertThatThrownBy(() -> combatService.demarrerCombat(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Un combat est déjà en cours");

    verify(ennemiRepository, never()).findById(anyLong());
  }

  @Test
  void shouldAttackEnemy() {
    ProgressionJoueur progression = progression();
    progression.setPuissance(4);

    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 5, 5);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemi("Bandit", 20, 3, 5, 5);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.vieEnnemiActuelle()).isEqualTo(13);
    assertThat(response.vieJoueurActuelle()).isEqualTo(27);
    assertThat(response.statut()).isEqualTo(StatutCombat.EN_COURS);
    assertThat(response.recompense()).isNull();
  }

  @Test
  void shouldHealPlayer() {
    ProgressionJoueur progression = progression();
    progression.setVieActuelle(20);

    Capacite soin = capacite(1L, TypeCapacite.SOIN, 5, 5);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(soin)));

    Ennemi ennemi = ennemi("Bandit", 20, 3, 5, 5);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.vieJoueurActuelle()).isEqualTo(22);
  }

  @Test
  void shouldWinCombatAndGainExperience() {
    ProgressionJoueur progression = progression();
    progression.setExperience(25);

    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 50, 50);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemi("Bandit", 20, 3, 10, 10);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.statut()).isEqualTo(StatutCombat.VICTOIRE);
    assertThat(response.recompense()).isNotNull();
    assertThat(response.recompense().experience()).isEqualTo(10);
    assertThat(progression.getExperience()).isEqualTo(35);
    assertThat(progression.getNiveau()).isEqualTo(2);
  }

  @Test
  void shouldLoseCombat() {
    ProgressionJoueur progression = progression();
    progression.setVieActuelle(2);

    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 1, 1);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemi("Bandit", 50, 10, 5, 5);
    Combat combat = combat(progression, ennemi, 50);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.statut()).isEqualTo(StatutCombat.DEFAITE);
    assertThat(response.vieJoueurActuelle()).isZero();
    assertThat(response.recompense()).isNull();
  }

  @Test
  void shouldFleeCombat() {
    ProgressionJoueur progression = progression();
    Ennemi ennemi = ennemi("Bandit", 20, 3, 5, 5);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));
    when(combatRepository.save(combat)).thenReturn(combat);

    CombatResponse response = combatService.fuirCombat();

    assertThat(response.statut()).isEqualTo(StatutCombat.FUITE);
    assertThat(response.recompense()).isNull();
  }

  private void mockProgressionConnectee(ProgressionJoueur progression) {
    Utilisateur utilisateur = utilisateur();

    when(utilisateurRepository.findByEmail("test@test.com")).thenReturn(Optional.of(utilisateur));
    when(progressionJoueurRepository.findByUtilisateur(utilisateur)).thenReturn(Optional.of(progression));
  }

  private Utilisateur utilisateur() {
    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setId(1L);
    utilisateur.setEmail("test@test.com");
    return utilisateur;
  }

  private ProgressionJoueur progression() {
    Personnage personnage = new Personnage();
    personnage.setId(1L);
    personnage.setNom("Luffy");

    ProgressionJoueur progression = new ProgressionJoueur();
    progression.setId(1L);
    progression.setUtilisateur(utilisateur());
    progression.setPersonnage(personnage);
    progression.setNiveau(1);
    progression.setExperience(0);
    progression.setPuissance(1);
    progression.setVieMax(30);
    progression.setVieActuelle(30);
    progression.setEnduranceMax(10);
    progression.setEnduranceActuelle(10);
    progression.setBerries(0);
    progression.setPrime(0L);

    return progression;
  }

  private Ennemi ennemi(String nom, int vieMax, int puissance, int experienceMin, int experienceMax) {
    Ennemi ennemi = new Ennemi();
    ennemi.setId(1L);
    ennemi.setNom(nom);
    ennemi.setVieMax(vieMax);
    ennemi.setPuissance(puissance);
    ennemi.setExperienceMin(experienceMin);
    ennemi.setExperienceMax(experienceMax);
    return ennemi;
  }

  private Capacite capacite(Long id, TypeCapacite typeCapacite, int valeurMin, int valeurMax) {
    Capacite capacite = new Capacite();
    capacite.setId(id);
    capacite.setNom("Capacite test");
    capacite.setTypeCapacite(typeCapacite);
    capacite.setValeurMin(valeurMin);
    capacite.setValeurMax(valeurMax);
    capacite.setCoutEndurance(1);
    return capacite;
  }

  private Combat combat(ProgressionJoueur progression, Ennemi ennemi, int vieEnnemiActuelle) {
    Combat combat = new Combat();
    combat.setId(1L);
    combat.setProgressionJoueur(progression);
    combat.setEnnemi(ennemi);
    combat.setVieEnnemiActuelle(vieEnnemiActuelle);
    combat.setStatut(StatutCombat.EN_COURS);
    return combat;
  }
}