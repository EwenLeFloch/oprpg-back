package com.onepiecerpg.api.service;

import java.security.SecureRandom;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.onepiecerpg.api.dto.CombatResponse;
import com.onepiecerpg.api.dto.RecompenseCombatResponse;
import com.onepiecerpg.api.entity.Capacite;
import com.onepiecerpg.api.entity.Combat;
import com.onepiecerpg.api.entity.Ennemi;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.StatutCombat;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.entity.Zone;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.CombatRepository;
import com.onepiecerpg.api.repository.EnnemiRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;
import com.onepiecerpg.api.repository.ZoneRepository;

@Service
public class CombatService {

  private static final int BONUS_VIE_MAX_PAR_NIVEAU = 2;
  private static final int BONUS_ENDURANCE_MAX_PAR_NIVEAU = 2;
  private static final int BONUS_PUISSANCE_PAR_NIVEAU = 1;

  private static final double EXP_MIN_FACTEUR = 6.0;
  private static final double EXP_MAX_FACTEUR = 9.0;
  private static final double PRIME_MIN_FACTEUR = 50.0;
  private static final double PRIME_MAX_FACTEUR = 80.0;
  private static final double MULTIPLICATEUR_BOSS = 2.5;

  private static final double EXP_BASE = 30.0;
  private static final double EXP_EXPOSANT = 1.645;

  private static final SecureRandom RANDOM = new SecureRandom();

  private final CombatRepository combatRepository;
  private final EnnemiRepository ennemiRepository;
  private final ProgressionJoueurRepository progressionJoueurRepository;
  private final UtilisateurRepository utilisateurRepository;
  private final ZoneRepository zoneRepository;

  public CombatService(
      CombatRepository combatRepository,
      EnnemiRepository ennemiRepository,
      ProgressionJoueurRepository progressionJoueurRepository,
      UtilisateurRepository utilisateurRepository,
      ZoneRepository zoneRepository) {
    this.combatRepository = combatRepository;
    this.ennemiRepository = ennemiRepository;
    this.progressionJoueurRepository = progressionJoueurRepository;
    this.utilisateurRepository = utilisateurRepository;
    this.zoneRepository = zoneRepository;
  }

  /**
   * Démarre un combat dans la zone donnée.
   * - Si le joueur a atteint le niveauRequis de la zone et que le boss n'est pas
   * encore vaincu
   * dans cette progression → le boss est imposé.
   * - Sinon → ennemi classique aléatoire parmi ceux de la zone.
   * - Si le boss a déjà été vaincu → zone verrouillée, exception.
   */
  public CombatResponse demarrerCombat(Long zoneId) {
    ProgressionJoueur progression = recupererProgressionConnectee();
    verifierAucunCombatEnCours(progression);

    Zone zone = recupererZone(zoneId);
    verifierAccesZone(progression, zone);

    Ennemi ennemi = choisirEnnemi(progression, zone);

    Combat combat = new Combat();
    combat.setProgressionJoueur(progression);
    combat.setEnnemi(ennemi);
    combat.setVieEnnemiActuelle(ennemi.getVieMax());

    return convertir(combatRepository.save(combat));
  }

  public CombatResponse recupererCombatEnCours() {
    return convertir(recupererCombatEnCoursConnecte());
  }

  public CombatResponse utiliserCapacite(Long capaciteId) {
    ProgressionJoueur progression = recupererProgressionConnectee();
    Combat combat = recupererCombatEnCours(progression);
    Capacite capacite = recupererCapaciteJoueur(progression, capaciteId);

    appliquerCapacite(combat, progression, capacite);
    RecompenseCombatResponse recompense = verifierFinCombat(combat, progression);

    combatRepository.save(combat);
    progressionJoueurRepository.save(progression);

    return convertir(combat, recompense);
  }

  public CombatResponse fuirCombat() {
    Combat combat = recupererCombatEnCoursConnecte();
    combat.setStatut(StatutCombat.FUITE);
    return convertir(combatRepository.save(combat));
  }

  // -------------------------------------------------------------------------
  // Choix de l'ennemi
  // -------------------------------------------------------------------------

  private Ennemi choisirEnnemi(ProgressionJoueur progression, Zone zone) {
    boolean bossDisponible = bossEstDisponible(progression, zone);
    boolean bossDejaVaincu = bossDejaVaincu(progression, zone);

    if (bossDejaVaincu) {
      throw new IllegalStateException("Le boss de cette zone a déjà été vaincu. Passez à la zone suivante.");
    }

    if (bossDisponible) {
      return ennemiRepository.findByZoneIdAndBossTrue(zone.getId())
          .orElseThrow(() -> new RessourceIntrouvableException("Boss introuvable dans cette zone"));
    }

    return choisirEnnemiAleatoire(zone);
  }

  /**
   * Le boss devient disponible quand le joueur atteint le niveauRequis de la
   * zone.
   * C'est ce niveau qui détermine l'accès au boss (= déblocage de l'île
   * suivante).
   */
  private boolean bossEstDisponible(ProgressionJoueur progression, Zone zone) {
    return progression.getNiveau() >= zone.getNiveauRequis()
        && ennemiRepository.findByZoneIdAndBossTrue(zone.getId()).isPresent();
  }

  /**
   * Vérifie si le joueur a déjà vaincu le boss de cette zone dans sa progression
   * actuelle.
   */
  private boolean bossDejaVaincu(ProgressionJoueur progression, Zone zone) {
    return ennemiRepository.findByZoneIdAndBossTrue(zone.getId())
        .map(boss -> combatRepository.existsByProgressionJoueurIdAndEnnemiIdAndStatut(
            progression.getId(), boss.getId(), StatutCombat.VICTOIRE))
        .orElse(false);
  }

  private Ennemi choisirEnnemiAleatoire(Zone zone) {
    List<Ennemi> ennemis = ennemiRepository.findByZoneIdAndBossFalse(zone.getId());
    if (ennemis.isEmpty()) {
      throw new RessourceIntrouvableException("Aucun ennemi disponible dans cette zone");
    }
    return ennemis.get(RANDOM.nextInt(ennemis.size()));
  }

  private void verifierAccesZone(ProgressionJoueur progression, Zone zone) {
    if (progression.getNiveau() < zone.getNiveauRequis()) {
      throw new IllegalStateException(
          "Niveau insuffisant pour accéder à cette zone (requis : " + zone.getNiveauRequis() + ")");
    }
  }

  // -------------------------------------------------------------------------
  // Logique de combat
  // -------------------------------------------------------------------------

  private void appliquerCapacite(Combat combat, ProgressionJoueur progression, Capacite capacite) {
    switch (capacite.getTypeCapacite()) {
      case ATTAQUE -> appliquerAttaque(combat, progression, capacite);
      case SOIN -> appliquerSoin(progression, capacite);
      default -> throw new IllegalArgumentException("Type de capacite non géré pour le moment");
    }
  }

  private void appliquerAttaque(Combat combat, ProgressionJoueur progression, Capacite capacite) {
    int degats = calculerDegats(progression, capacite);
    combat.setVieEnnemiActuelle(Math.max(0, combat.getVieEnnemiActuelle() - degats));
  }

  private void appliquerSoin(ProgressionJoueur progression, Capacite capacite) {
    int soin = valeurAleatoireEntre(capacite.getValeurMin(), capacite.getValeurMax());
    progression.setVieActuelle(Math.min(progression.getVieMax(), progression.getVieActuelle() + soin));
  }

  private RecompenseCombatResponse verifierFinCombat(Combat combat, ProgressionJoueur progression) {
    if (ennemiEstVaincu(combat)) {
      return appliquerVictoire(combat, progression);
    }
    appliquerTourEnnemi(combat, progression);
    if (joueurEstVaincu(progression)) {
      combat.setStatut(StatutCombat.DEFAITE);
    }
    return null;
  }

  private RecompenseCombatResponse appliquerVictoire(Combat combat, ProgressionJoueur progression) {
    combat.setStatut(StatutCombat.VICTOIRE);

    int niveauZone = combat.getEnnemi().getZone().getNiveauRequis();
    boolean boss = combat.getEnnemi().isBoss();

    int exp = calculerRecompense(niveauZone, EXP_MIN_FACTEUR, EXP_MAX_FACTEUR, boss);
    long prime = calculerRecompense(niveauZone, PRIME_MIN_FACTEUR, PRIME_MAX_FACTEUR, boss);

    appliquerExperience(progression, exp);
    progression.setPrime(progression.getPrime() + prime);

    return new RecompenseCombatResponse(exp, prime);
  }

  private void appliquerTourEnnemi(Combat combat, ProgressionJoueur progression) {
    int nouvelleVie = progression.getVieActuelle() - combat.getEnnemi().getPuissance();
    progression.setVieActuelle(Math.max(0, nouvelleVie));
  }

  private boolean ennemiEstVaincu(Combat combat) {
    return combat.getVieEnnemiActuelle() <= 0;
  }

  private boolean joueurEstVaincu(ProgressionJoueur progression) {
    return progression.getVieActuelle() <= 0;
  }

  // -------------------------------------------------------------------------
  // Formules
  // -------------------------------------------------------------------------

  private int calculerRecompense(int niveauZone, double minFacteur, double maxFacteur, boolean boss) {
    double mult = boss ? MULTIPLICATEUR_BOSS : 1.0;
    int min = (int) Math.round(minFacteur * niveauZone * mult);
    int max = (int) Math.round(maxFacteur * niveauZone * mult);
    return valeurAleatoireEntre(min, max);
  }

  int experienceRequise(int niveau) {
    return (int) Math.round(EXP_BASE * Math.pow(niveau, EXP_EXPOSANT));
  }

  private void appliquerExperience(ProgressionJoueur progression, int experienceGagnee) {
    progression.setExperience(progression.getExperience() + experienceGagnee);
    while (peutMonterDeNiveau(progression)) {
      monterDeNiveau(progression);
    }
  }

  private boolean peutMonterDeNiveau(ProgressionJoueur progression) {
    return progression.getExperience() >= experienceRequise(progression.getNiveau());
  }

  private void monterDeNiveau(ProgressionJoueur progression) {
    progression.setNiveau(progression.getNiveau() + 1);
    progression.setPuissance(progression.getPuissance() + BONUS_PUISSANCE_PAR_NIVEAU);
    progression.setVieMax(progression.getVieMax() + BONUS_VIE_MAX_PAR_NIVEAU);
    progression.setVieActuelle(progression.getVieMax());
    progression.setEnduranceMax(progression.getEnduranceMax() + BONUS_ENDURANCE_MAX_PAR_NIVEAU);
    progression.setEnduranceActuelle(progression.getEnduranceMax());
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private int calculerDegats(ProgressionJoueur progression, Capacite capacite) {
    return valeurAleatoireEntre(capacite.getValeurMin(), capacite.getValeurMax())
        + bonusPuissance(progression.getPuissance());
  }

  private int bonusPuissance(int puissance) {
    return (int) Math.sqrt(puissance);
  }

  private int valeurAleatoireEntre(int min, int max) {
    if (min > max)
      throw new IllegalArgumentException("min ne peut pas être supérieur à max");
    return RANDOM.nextInt(min, max + 1);
  }

  private void verifierAucunCombatEnCours(ProgressionJoueur progression) {
    combatRepository.findByProgressionJoueurIdAndStatut(progression.getId(), StatutCombat.EN_COURS)
        .ifPresent(c -> {
          throw new IllegalStateException("Un combat est déjà en cours");
        });
  }

  private Combat recupererCombatEnCoursConnecte() {
    return recupererCombatEnCours(recupererProgressionConnectee());
  }

  private Combat recupererCombatEnCours(ProgressionJoueur progression) {
    return combatRepository.findByProgressionJoueurIdAndStatut(progression.getId(), StatutCombat.EN_COURS)
        .orElseThrow(() -> new RessourceIntrouvableException("Aucun combat en cours"));
  }

  private Zone recupererZone(Long zoneId) {
    return zoneRepository.findById(zoneId)
        .orElseThrow(() -> new RessourceIntrouvableException("Zone introuvable"));
  }

  private Capacite recupererCapaciteJoueur(ProgressionJoueur progression, Long capaciteId) {
    return progression.getPersonnage().getCapacites().stream()
        .filter(c -> c.getId().equals(capaciteId))
        .findFirst()
        .orElseThrow(() -> new RessourceIntrouvableException("Capacite introuvable"));
  }

  private ProgressionJoueur recupererProgressionConnectee() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
        .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur connecté introuvable"));
    return progressionJoueurRepository.findByUtilisateur(utilisateur)
        .orElseThrow(() -> new RessourceIntrouvableException("Progression du joueur non trouvée"));
  }

  private CombatResponse convertir(Combat combat) {
    return convertir(combat, null);
  }

  private CombatResponse convertir(Combat combat, RecompenseCombatResponse recompense) {
    return new CombatResponse(
        combat.getId(),
        combat.getEnnemi().getNom(),
        combat.getVieEnnemiActuelle(),
        combat.getProgressionJoueur().getVieActuelle(),
        combat.getStatut(),
        recompense);
  }
}