package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.onepiecerpg.api.dto.CombatResponse;
import com.onepiecerpg.api.entity.*;
import com.onepiecerpg.api.repository.*;

class CombatServiceTest {

  private CombatRepository combatRepository;
  private EnnemiRepository ennemiRepository;
  private ProgressionJoueurRepository progressionJoueurRepository;
  private UtilisateurRepository utilisateurRepository;
  private ZoneRepository zoneRepository;
  private CombatService combatService;

  @BeforeEach
  void setUp() {
    combatRepository = mock(CombatRepository.class);
    ennemiRepository = mock(EnnemiRepository.class);
    progressionJoueurRepository = mock(ProgressionJoueurRepository.class);
    utilisateurRepository = mock(UtilisateurRepository.class);
    zoneRepository = mock(ZoneRepository.class);

    combatService = new CombatService(
        combatRepository, ennemiRepository,
        progressionJoueurRepository, utilisateurRepository, zoneRepository);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("test@test.com", null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void experienceRequise_shouldBeCorrectAtLevel1() {
    assertThat(combatService.experienceRequise(1)).isEqualTo(30);
  }

  @Test
  void experienceRequise_shouldGrowWithLevel() {
    assertThat(combatService.experienceRequise(10))
        .isGreaterThan(combatService.experienceRequise(1));
    assertThat(combatService.experienceRequise(50))
        .isGreaterThan(combatService.experienceRequise(10));
  }

  @Test
  void shouldStartCombatWithRandomEnemy() {
    ProgressionJoueur progression = progression();
    Zone zone = zone(1);
    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone, 3);

    mockProgressionConnectee(progression);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());
    when(ennemiRepository.findByZoneIdAndBossTrue(1L)).thenReturn(Optional.empty());
    when(ennemiRepository.findByZoneIdAndBossFalse(1L)).thenReturn(List.of(ennemi));
    when(combatRepository.save(any())).thenAnswer(inv -> {
      Combat c = inv.getArgument(0);
      c.setId(1L);
      return c;
    });

    CombatResponse response = combatService.demarrerCombat(1L);

    assertThat(response.ennemi()).isEqualTo("Bandit");
    assertThat(response.statut()).isEqualTo(StatutCombat.EN_COURS);
  }

  @Test
  void shouldRejectStartWhenNoEndurance() {
    ProgressionJoueur progression = progression();
    progression.setEnduranceActuelle(0);
    Zone zone = zone(1);

    mockProgressionConnectee(progression);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> combatService.demarrerCombat(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Endurance insuffisante pour lancer un combat");
  }

  @Test
  void shouldRejectStartWhenLevelTooLow() {
    ProgressionJoueur progression = progression();
    Zone zone = zone(5);

    mockProgressionConnectee(progression);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> combatService.demarrerCombat(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Niveau insuffisant");
  }

  @Test
  void shouldImposeBossWhenLevelReached() {
    ProgressionJoueur progression = progression();
    progression.setNiveau(5);
    Zone zone = zone(1);
    Ennemi boss = ennemiSansCapacite("Higuma", true, zone, 6);
    boss.setNiveauRequis(5);

    mockProgressionConnectee(progression);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());
    when(ennemiRepository.findByZoneIdAndBossTrue(1L)).thenReturn(Optional.of(boss));
    when(combatRepository.existsByProgressionJoueurIdAndEnnemiIdAndStatut(1L, 1L, StatutCombat.VICTOIRE))
        .thenReturn(false);
    when(combatRepository.save(any())).thenAnswer(inv -> {
      Combat c = inv.getArgument(0);
      c.setId(1L);
      return c;
    });

    assertThat(combatService.demarrerCombat(1L).ennemi()).isEqualTo("Higuma");
  }

  @Test
  void shouldNotImposeBossWhenPlayerLevelBelowBossRequirement() {
    ProgressionJoueur progression = progression();
    Zone zone = zone(1);
    Ennemi boss = ennemiSansCapacite("Higuma", true, zone, 6);
    boss.setNiveauRequis(5);
    Ennemi bandit = ennemiSansCapacite("Bandit", false, zone, 3);

    mockProgressionConnectee(progression);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());
    when(ennemiRepository.findByZoneIdAndBossTrue(1L)).thenReturn(Optional.of(boss));
    when(ennemiRepository.findByZoneIdAndBossFalse(1L)).thenReturn(List.of(bandit));
    when(combatRepository.save(any())).thenAnswer(inv -> {
      Combat c = inv.getArgument(0);
      c.setId(1L);
      return c;
    });

    CombatResponse response = combatService.demarrerCombat(1L);

    assertThat(response.ennemi()).isEqualTo("Bandit");
  }

  @Test
  void shouldBlockZoneWhenBossAlreadyDefeated() {
    ProgressionJoueur progression = progression();
    progression.setNiveau(5);
    Zone zone = zone(5);
    Ennemi boss = ennemiSansCapacite("Higuma", true, zone, 6);

    mockProgressionConnectee(progression);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());
    when(ennemiRepository.findByZoneIdAndBossTrue(1L)).thenReturn(Optional.of(boss));
    when(combatRepository.existsByProgressionJoueurIdAndEnnemiIdAndStatut(1L, 1L, StatutCombat.VICTOIRE))
        .thenReturn(true);

    assertThatThrownBy(() -> combatService.demarrerCombat(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("boss");
  }

  @Test
  void shouldAttackEnemy() {
    ProgressionJoueur progression = progression();
    progression.setPuissance(1);

    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 5, 5);
    attaque.setPrecision(100);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 3);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.vieEnnemiActuelle()).isEqualTo(14);
    assertThat(response.vieJoueurActuelle()).isEqualTo(27);
    assertThat(response.statut()).isEqualTo(StatutCombat.EN_COURS);
  }

  @Test
  void shouldHealPlayer() {
    ProgressionJoueur progression = progression();
    progression.setVieActuelle(20);

    Capacite soin = capacite(1L, TypeCapacite.SOIN, 5, 5);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(soin)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 3);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.vieJoueurActuelle()).isEqualTo(22);
  }

  @Test
  void shouldWinCombatAndGainRewards() {
    ProgressionJoueur progression = progression();
    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 50, 50);
    attaque.setPrecision(100);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Zone zone = zone(5);
    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone, 3);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.statut()).isEqualTo(StatutCombat.VICTOIRE);
    assertThat(response.recompense().experience()).isBetween(30, 45);
    assertThat(response.recompense().prime()).isBetween(250L, 400L);
  }

  @Test
  void shouldLoseCombat() {
    ProgressionJoueur progression = progression();
    progression.setVieActuelle(2);

    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 1, 1);
    attaque.setPrecision(0);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 10);
    Combat combat = combat(progression, ennemi, 50);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.statut()).isEqualTo(StatutCombat.DEFAITE);
    assertThat(response.vieJoueurActuelle()).isZero();
  }

  @Test
  void shouldFleeCombat() {
    ProgressionJoueur progression = progression();
    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 3);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));
    when(combatRepository.save(combat)).thenReturn(combat);

    assertThat(combatService.fuirCombat().statut()).isEqualTo(StatutCombat.FUITE);
  }

  @Test
  void shouldConsumeEndurance() {
    ProgressionJoueur progression = progression();
    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 1, 1);
    attaque.setCoutEndurance(3);
    attaque.setPrecision(100);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 0);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    assertThat(combatService.utiliserCapacite(1L).enduranceActuelle()).isEqualTo(7);
  }

  @Test
  void shouldRejectWhenNotEnoughEndurance() {
    ProgressionJoueur progression = progression();
    progression.setEnduranceActuelle(1);
    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 1, 1);
    attaque.setCoutEndurance(3);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 3);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    assertThatThrownBy(() -> combatService.utiliserCapacite(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Endurance insuffisante");
  }

  @Test
  void shouldApplyBoostAndMultiplyDamage() {
    ProgressionJoueur progression = progression();
    progression.setPuissance(1);

    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 5, 5);
    attaque.setPrecision(100);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 0);
    Combat combat = combat(progression, ennemi, 20);
    combat.setBoostMultiplicateurJoueur(1.3);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.vieEnnemiActuelle()).isEqualTo(12);
  }

  @Test
  void shouldEsquiverEnemyAttack() {
    ProgressionJoueur progression = progression();
    int vieInitiale = progression.getVieActuelle();

    Capacite esquive = capacite(1L, TypeCapacite.ESQUIVE, 0, 0);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(esquive)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 5);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    assertThat(combatService.utiliserCapacite(1L).vieJoueurActuelle()).isEqualTo(vieInitiale);
  }

  @Test
  void shouldContreEnemyAttack() {
    ProgressionJoueur progression = progression();
    progression.setPuissance(1);
    int vieInitiale = progression.getVieActuelle();

    Capacite contre = capacite(1L, TypeCapacite.CONTRE, 4, 4);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(contre)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 5);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.vieJoueurActuelle()).isEqualTo(vieInitiale);
    assertThat(response.vieEnnemiActuelle()).isEqualTo(15);
  }

  @Test
  void shouldParalyseEnemy() {
    ProgressionJoueur progression = progression();
    int vieInitiale = progression.getVieActuelle();

    Capacite paralysie = capacite(1L, TypeCapacite.PARALYSIE, 0, 0);
    paralysie.setDuree(2);
    paralysie.setPrecision(100);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(paralysie)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 5);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.vieJoueurActuelle()).isEqualTo(vieInitiale);
    assertThat(combat.getToursParalysieEnnemi()).isEqualTo(1);
  }

  @Test
  void shouldRejectWhenPlayerIsParalysed() {
    ProgressionJoueur progression = progression();
    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 5, 5);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 3);
    Combat combat = combat(progression, ennemi, 20);
    combat.setToursParalysieJoueur(1);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    assertThatThrownBy(() -> combatService.utiliserCapacite(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("paralysé");
  }

  @Test
  void shouldEsquiverParalysieJoueur() {
    ProgressionJoueur progression = progression();

    Capacite paralysie = capacite(1L, TypeCapacite.PARALYSIE, 0, 0);
    paralysie.setDuree(2);
    paralysie.setPrecision(100);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(paralysie)));

    Capacite esquiveEnnemi = capacite(99L, TypeCapacite.ESQUIVE, 0, 0);
    Ennemi ennemi = ennemiAvecCapacite("Bandit", false, zone(1), 3, esquiveEnnemi);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    combatService.utiliserCapacite(1L);

    assertThat(combat.getToursParalysieEnnemi()).isZero();
  }

  @Test
  void shouldRenvoiRetournerAttaqueEnnemi() {
    ProgressionJoueur progression = progression();
    int vieInitiale = progression.getVieActuelle();

    Capacite renvoi = capacite(1L, TypeCapacite.RENVOI, 0, 0);
    renvoi.setPrecision(100);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(renvoi)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 5);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.vieJoueurActuelle()).isEqualTo(vieInitiale);
    assertThat(response.vieEnnemiActuelle()).isEqualTo(15);
  }

  @Test
  void shouldBossGiveMoreRewards() {
    ProgressionJoueur progression = progression();
    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 50, 50);
    attaque.setPrecision(100);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Zone zone = zone(5);
    Ennemi boss = ennemiSansCapacite("Higuma", true, zone, 0);
    Combat combat = combat(progression, boss, 5);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.statut()).isEqualTo(StatutCombat.VICTOIRE);
    assertThat(response.recompense().experience()).isBetween(75, 113);
    assertThat(response.recompense().prime()).isBetween(625L, 1000L);
  }

  @Test
  void shouldExposeEnnemiIdEtatEtHistoriqueApresAttaque() {
    ProgressionJoueur progression = progression();
    progression.setPuissance(1);

    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 5, 5);
    attaque.setNom("Gomu Gomu no Pistol");
    attaque.setPrecision(100);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 3);
    ennemi.setId(7L);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.ennemiId()).isEqualTo(7L);
    assertThat(response.etatJoueur()).isEqualTo(EtatCombat.NORMAL);
    assertThat(response.etatEnnemi()).isEqualTo(EtatCombat.NORMAL);
    assertThat(response.historique())
        .contains("Luffy utilise Gomu Gomu no Pistol")
        .anyMatch(ligne -> ligne.contains("dégâts à Bandit"));
  }

  @Test
  void shouldExposeEtatParalyseApresParalysieReussie() {
    ProgressionJoueur progression = progression();

    Capacite paralysie = capacite(1L, TypeCapacite.PARALYSIE, 0, 0);
    paralysie.setDuree(2);
    paralysie.setPrecision(100);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(paralysie)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 5);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.etatEnnemi()).isEqualTo(EtatCombat.PARALYSE);
  }

  @Test
  void shouldAccumulerHistoriqueSurPlusieursTours() {
    ProgressionJoueur progression = progression();
    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 1, 1);
    attaque.setPrecision(100);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone(1), 0);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    combatService.utiliserCapacite(1L);
    CombatResponse second = combatService.utiliserCapacite(1L);

    assertThat(second.historique()).hasSizeGreaterThan(3);
  }

  @Test
  void shouldStarterCombatAvecLigneHistoriqueInitiale() {
    ProgressionJoueur progression = progression();
    Zone zone = zone(1);
    Ennemi ennemi = ennemiSansCapacite("Bandit", false, zone, 3);

    mockProgressionConnectee(progression);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());
    when(ennemiRepository.findByZoneIdAndBossTrue(1L)).thenReturn(Optional.empty());
    when(ennemiRepository.findByZoneIdAndBossFalse(1L)).thenReturn(List.of(ennemi));
    when(combatRepository.save(any())).thenAnswer(inv -> {
      Combat c = inv.getArgument(0);
      c.setId(1L);
      return c;
    });

    CombatResponse response = combatService.demarrerCombat(1L);

    assertThat(response.historique()).containsExactly("Combat engagé contre Bandit");
  }

  private void mockProgressionConnectee(ProgressionJoueur progression) {
    Utilisateur utilisateur = utilisateur();
    when(utilisateurRepository.findByEmail("test@test.com")).thenReturn(Optional.of(utilisateur));
    when(progressionJoueurRepository.findByUtilisateur(utilisateur)).thenReturn(Optional.of(progression));
  }

  private Utilisateur utilisateur() {
    Utilisateur u = new Utilisateur();
    u.setId(1L);
    u.setEmail("test@test.com");
    return u;
  }

  private Zone zone(int niveauRequis) {
    Ile ile = new Ile();
    ile.setId(1L);
    ile.setNom("Dawn Island");
    ile.setNiveauRequis(1);

    Zone zone = new Zone();
    zone.setId(1L);
    zone.setNom("Village Fuschia");
    zone.setNiveauRequis(niveauRequis);
    zone.setIle(ile);
    return zone;
  }

  private ProgressionJoueur progression() {
    Personnage personnage = new Personnage();
    personnage.setId(1L);
    personnage.setNom("Luffy");
    personnage.setCapacites(new HashSet<>());

    ProgressionJoueur p = new ProgressionJoueur();
    p.setId(1L);
    p.setUtilisateur(utilisateur());
    p.setPersonnage(personnage);
    p.setNiveau(1);
    p.setExperience(0);
    p.setPuissance(1);
    p.setVieMax(30);
    p.setVieActuelle(30);
    p.setEnduranceMax(10);
    p.setEnduranceActuelle(10);
    p.setBerries(0);
    p.setPrime(0L);
    return p;
  }

  private Ennemi ennemiSansCapacite(String nom, boolean boss, Zone zone, int puissance) {
    Ennemi e = new Ennemi();
    e.setId(1L);
    e.setNom(nom);
    e.setVieMax(20);
    e.setPuissance(puissance);
    e.setBoss(boss);
    e.setZone(zone);
    e.setCapacites(new HashSet<>());
    return e;
  }

  private Ennemi ennemiAvecCapacite(String nom, boolean boss, Zone zone, int puissance,
      Capacite capacite) {
    Ennemi e = ennemiSansCapacite(nom, boss, zone, puissance);
    e.setCapacites(new HashSet<>(Set.of(capacite)));
    return e;
  }

  private Capacite capacite(Long id, TypeCapacite type, int valeurMin, int valeurMax) {
    Capacite c = new Capacite();
    c.setId(id);
    c.setNom("Capacite test");
    c.setTypeCapacite(type);
    c.setValeurMin(valeurMin);
    c.setValeurMax(valeurMax);
    c.setDuree(1);
    c.setPrecision(100);
    c.setCoutEndurance(1);
    return c;
  }

  private Combat combat(ProgressionJoueur progression, Ennemi ennemi, int vieEnnemiActuelle) {
    Combat combat = new Combat();
    combat.setId(1L);
    combat.setProgressionJoueur(progression);
    combat.setEnnemi(ennemi);
    combat.setVieEnnemiActuelle(vieEnnemiActuelle);
    combat.setStatut(StatutCombat.EN_COURS);
    combat.setBoostMultiplicateurJoueur(1.0);
    combat.setBoostMultiplicateurEnnemi(1.0);
    combat.setToursParalysieJoueur(0);
    combat.setToursParalysieEnnemi(0);
    return combat;
  }
}