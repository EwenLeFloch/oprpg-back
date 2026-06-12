package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        combatRepository,
        ennemiRepository,
        progressionJoueurRepository,
        utilisateurRepository,
        zoneRepository);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("test@test.com", null));
  }

  @BeforeEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  // -------------------------------------------------------------------------
  // Tests formule XP
  // -------------------------------------------------------------------------

  @Test
  void experienceRequise_shouldBeCorrectAtLevel1() {
    // 30 * 1^1.645 = 30
    assertThat(combatService.experienceRequise(1)).isEqualTo(30);
  }

  @Test
  void experienceRequise_shouldGrowWithLevel() {
    int xpNiveau1 = combatService.experienceRequise(1);
    int xpNiveau10 = combatService.experienceRequise(10);
    int xpNiveau50 = combatService.experienceRequise(50);

    assertThat(xpNiveau10).isGreaterThan(xpNiveau1);
    assertThat(xpNiveau50).isGreaterThan(xpNiveau10);
  }

  // -------------------------------------------------------------------------
  // Tests combat
  // -------------------------------------------------------------------------

  // Remplacer shouldStartCombat et ajouter les nouveaux tests
  // (uniquement les méthodes modifiées/ajoutées — le reste du fichier est
  // identique au point 1)

  @Test
  void shouldStartCombatWithRandomEnemy() {
    ProgressionJoueur progression = progression(); // niveau 1
    Zone zone = zone(1);
    Ennemi ennemi = ennemi("Bandit", false, zone);

    mockProgressionConnectee(progression);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());
    when(ennemiRepository.findByZoneIdAndBossTrue(1L)).thenReturn(Optional.empty());
    when(ennemiRepository.findByZoneIdAndBossFalse(1L)).thenReturn(List.of(ennemi));
    when(combatRepository.save(any(Combat.class))).thenAnswer(inv -> {
      Combat c = inv.getArgument(0);
      c.setId(1L);
      return c;
    });

    CombatResponse response = combatService.demarrerCombat(1L);

    assertThat(response.ennemi()).isEqualTo("Bandit");
    assertThat(response.statut()).isEqualTo(StatutCombat.EN_COURS);
  }

  @Test
  void shouldImposeBossWhenLevelReached() {
    ProgressionJoueur progression = progression();
    progression.setNiveau(5); // niveau requis de la zone = 5

    Zone zone = zone(5);
    Ennemi boss = ennemi("Higuma", true, zone);

    mockProgressionConnectee(progression);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());
    when(ennemiRepository.findByZoneIdAndBossTrue(1L)).thenReturn(Optional.of(boss));
    when(combatRepository.existsByProgressionJoueurIdAndEnnemiIdAndStatut(1L, 1L, StatutCombat.VICTOIRE))
        .thenReturn(false);
    when(combatRepository.save(any(Combat.class))).thenAnswer(inv -> {
      Combat c = inv.getArgument(0);
      c.setId(1L);
      return c;
    });

    CombatResponse response = combatService.demarrerCombat(1L);

    assertThat(response.ennemi()).isEqualTo("Higuma");
  }

  @Test
  void shouldBlockZoneWhenBossAlreadyDefeated() {
    ProgressionJoueur progression = progression();
    progression.setNiveau(5);

    Zone zone = zone(5);
    Ennemi boss = ennemi("Higuma", true, zone);

    mockProgressionConnectee(progression);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());
    when(ennemiRepository.findByZoneIdAndBossTrue(1L)).thenReturn(Optional.of(boss));
    when(combatRepository.existsByProgressionJoueurIdAndEnnemiIdAndStatut(1L, 1L, StatutCombat.VICTOIRE))
        .thenReturn(true); // boss déjà vaincu

    assertThatThrownBy(() -> combatService.demarrerCombat(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("boss");
  }

  @Test
  void shouldRejectAccessWhenLevelTooLow() {
    ProgressionJoueur progression = progression(); // niveau 1
    Zone zone = zone(5); // niveau requis 5

    mockProgressionConnectee(progression);
    when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> combatService.demarrerCombat(1L))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Niveau insuffisant");
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
    progression.setPuissance(4); // bonus sqrt(4) = 2

    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 5, 5);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemi("Bandit", false, zone(1));
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    // dégâts = 5 + sqrt(4) = 7 → vie ennemi = 20 - 7 = 13
    // tour ennemi : puissance ennemi = 3 → vie joueur = 30 - 3 = 27
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

    Ennemi ennemi = ennemi("Bandit", false, zone(1));
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    // soin = 5, puis tour ennemi - 3 → 20 + 5 - 3 = 22
    assertThat(response.vieJoueurActuelle()).isEqualTo(22);
  }

  @Test
  void shouldWinCombatAndGainRewards() {
    ProgressionJoueur progression = progression();
    progression.setExperience(0);

    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 50, 50);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Zone zone = zone(5); // niveauRequis = 5
    Ennemi ennemi = ennemi("Bandit", false, zone);
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.statut()).isEqualTo(StatutCombat.VICTOIRE);
    assertThat(response.recompense()).isNotNull();
    // xpMin = 6*5=30, xpMax = 9*5=45
    assertThat(response.recompense().experience()).isBetween(30, 45);
    // primeMin = 50*5=250, primeMax = 80*5=400
    assertThat(response.recompense().prime()).isBetween(250L, 400L);
  }

  @Test
  void shouldApplyBossMultiplierOnRewards() {
    ProgressionJoueur progression = progression();

    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 50, 50);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Zone zone = zone(5);
    Ennemi boss = ennemi("Higuma", true, zone); // boss = true
    Combat combat = combat(progression, boss, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));

    CombatResponse response = combatService.utiliserCapacite(1L);

    assertThat(response.statut()).isEqualTo(StatutCombat.VICTOIRE);
    // xpMin boss = round(6*5*2.5)=75, xpMax boss = round(9*5*2.5)=113
    assertThat(response.recompense().experience()).isBetween(75, 113);
    assertThat(response.recompense().prime()).isBetween(625L, 1000L);
  }

  @Test
  void shouldLoseCombat() {
    ProgressionJoueur progression = progression();
    progression.setVieActuelle(2);

    Capacite attaque = capacite(1L, TypeCapacite.ATTAQUE, 1, 1);
    progression.getPersonnage().setCapacites(new HashSet<>(Set.of(attaque)));

    Ennemi ennemi = ennemi("Bandit", false, zone(1));
    ennemi.setPuissance(10);
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
    Ennemi ennemi = ennemi("Bandit", false, zone(1));
    Combat combat = combat(progression, ennemi, 20);

    mockProgressionConnectee(progression);
    when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
        .thenReturn(Optional.of(combat));
    when(combatRepository.save(combat)).thenReturn(combat);

    CombatResponse response = combatService.fuirCombat();

    assertThat(response.statut()).isEqualTo(StatutCombat.FUITE);
    assertThat(response.recompense()).isNull();
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

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

  private Ennemi ennemi(String nom, boolean boss, Zone zone) {
    Ennemi e = new Ennemi();
    e.setId(1L);
    e.setNom(nom);
    e.setVieMax(20);
    e.setPuissance(3);
    e.setBoss(boss);
    e.setZone(zone);
    return e;
  }

  private Capacite capacite(Long id, TypeCapacite type, int valeurMin, int valeurMax) {
    Capacite c = new Capacite();
    c.setId(id);
    c.setNom("Capacite test");
    c.setTypeCapacite(type);
    c.setValeurMin(valeurMin);
    c.setValeurMax(valeurMax);
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
    return combat;
  }
}