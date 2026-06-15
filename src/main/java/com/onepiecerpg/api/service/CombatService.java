package com.onepiecerpg.api.service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.onepiecerpg.api.dto.CombatResponse;
import com.onepiecerpg.api.dto.RecompenseCombatResponse;
import com.onepiecerpg.api.dto.TourResultat;
import com.onepiecerpg.api.entity.Capacite;
import com.onepiecerpg.api.entity.Combat;
import com.onepiecerpg.api.entity.Ennemi;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.StatutCombat;
import com.onepiecerpg.api.entity.TypeCapacite;
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
  private static final long ID_BOSS_HIGUMA = 1L;

  private static final double EXP_BASE = 30.0;
  private static final double EXP_EXPOSANT = 1.645;
  private static final double BOOST_MULTIPLICATEUR = 1.3;

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

  public CombatResponse demarrerCombat(Long zoneId) {
    ProgressionJoueur progression = recupererProgressionConnectee();
    verifierAucunCombatEnCours(progression);
    verifierEnduranceSuffisante(progression);

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
    Capacite capaciteJoueur = recupererCapaciteJoueur(progression, capaciteId);

    verifierCoutEndurance(progression, capaciteJoueur);
    verifierNonParalyse(combat);

    TourResultat actionJoueur = calculerAction(
        capaciteJoueur, progression.getPuissance(), combat.getBoostMultiplicateurJoueur());
    TourResultat actionEnnemi = calculerActionEnnemi(combat);

    consommerEndurance(progression, capaciteJoueur);

    if (actionJoueur.reussi() && capaciteJoueur.getTypeCapacite() == TypeCapacite.BOOST) {
      combat.setBoostMultiplicateurJoueur(
          combat.getBoostMultiplicateurJoueur() * BOOST_MULTIPLICATEUR);
    }
    if (actionEnnemi.reussi() && actionEnnemi.type() == TypeCapacite.BOOST) {
      combat.setBoostMultiplicateurEnnemi(
          combat.getBoostMultiplicateurEnnemi() * BOOST_MULTIPLICATEUR);
    }

    appliquerActionSurEnnemi(actionJoueur, actionEnnemi, combat);
    appliquerActionSurJoueur(actionEnnemi, actionJoueur, combat, progression);

    if (combat.getToursParalysieJoueur() > 0) {
      combat.setToursParalysieJoueur(combat.getToursParalysieJoueur() - 1);
    }
    if (combat.getToursParalysieEnnemi() > 0) {
      combat.setToursParalysieEnnemi(combat.getToursParalysieEnnemi() - 1);
    }

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

  private TourResultat calculerAction(Capacite capacite, int puissance, double boostMultiplicateur) {
    if (!capaciteAtteintSaCible(capacite)) {
      return TourResultat.rate();
    }

    int valeurBase = valeurAleatoireEntre(capacite.getValeurMin(), capacite.getValeurMax());

    return switch (capacite.getTypeCapacite()) {
      case ATTAQUE -> {
        int degats = (int) Math.round(
            (valeurBase + bonusPuissance(puissance)) * boostMultiplicateur);
        yield new TourResultat(TypeCapacite.ATTAQUE, degats, 0, true);
      }
      case SOIN -> new TourResultat(TypeCapacite.SOIN, valeurBase, 0, true);
      case BOOST -> new TourResultat(TypeCapacite.BOOST, 0, 0, true);
      case ESQUIVE -> new TourResultat(TypeCapacite.ESQUIVE, 0, 0, true);
      case CONTRE -> {
        int degats = valeurBase + bonusPuissance(puissance);
        yield new TourResultat(TypeCapacite.CONTRE, degats, 0, true);
      }
      case RENVOI -> new TourResultat(TypeCapacite.RENVOI, 0, 0, true);
      case PARALYSIE -> new TourResultat(TypeCapacite.PARALYSIE, 0, capacite.getDuree(), true);
    };
  }

  private TourResultat calculerActionEnnemi(Combat combat) {
    if (combat.getToursParalysieEnnemi() > 0) {
      return new TourResultat(TypeCapacite.PARALYSIE, 0, 0, false);
    }

    List<Capacite> capacites = new ArrayList<>(combat.getEnnemi().getCapacites());
    if (capacites.isEmpty()) {
      int degats = combat.getEnnemi().getPuissance();
      return new TourResultat(TypeCapacite.ATTAQUE, degats, 0, true);
    }

    Capacite capaciteChoisie = capacites.get(RANDOM.nextInt(capacites.size()));
    return calculerAction(capaciteChoisie, combat.getEnnemi().getPuissance(),
        combat.getBoostMultiplicateurEnnemi());
  }

  private void appliquerActionSurEnnemi(TourResultat actionJoueur, TourResultat actionEnnemi,
      Combat combat) {
    if (!actionJoueur.reussi())
      return;

    boolean actionOffensive = estOffensif(actionJoueur.type());

    if (actionOffensive && actionEnnemi.reussi() && actionEnnemi.type() == TypeCapacite.RENVOI) {
      return;
    }

    if (actionOffensive && actionEnnemi.reussi()
        && (actionEnnemi.type() == TypeCapacite.ESQUIVE
            || actionEnnemi.type() == TypeCapacite.CONTRE)) {
      return;
    }

    if (actionJoueur.type() == TypeCapacite.ATTAQUE) {
      combat.setVieEnnemiActuelle(
          Math.max(0, combat.getVieEnnemiActuelle() - actionJoueur.valeur()));
    } else if (actionJoueur.type() == TypeCapacite.PARALYSIE) {
      combat.setToursParalysieEnnemi(
          Math.max(combat.getToursParalysieEnnemi(), actionJoueur.duree()));
    }
  }

  private void appliquerActionSurJoueur(TourResultat actionEnnemi, TourResultat actionJoueur,
      Combat combat, ProgressionJoueur progression) {

    if (actionJoueur.reussi() && actionJoueur.type() == TypeCapacite.SOIN) {
      progression.setVieActuelle(
          Math.min(progression.getVieMax(), progression.getVieActuelle() + actionJoueur.valeur()));
    }

    if (actionJoueur.reussi() && actionJoueur.type() == TypeCapacite.RENVOI
        && actionEnnemi.reussi() && actionEnnemi.type() == TypeCapacite.ATTAQUE) {
      combat.setVieEnnemiActuelle(
          Math.max(0, combat.getVieEnnemiActuelle() - actionEnnemi.valeur()));
      return;
    }

    if (actionEnnemi.reussi() && actionEnnemi.type() == TypeCapacite.RENVOI
        && actionJoueur.reussi() && actionJoueur.type() == TypeCapacite.ATTAQUE) {
      progression.setVieActuelle(
          Math.max(0, progression.getVieActuelle() - actionJoueur.valeur()));
      return;
    }

    if (!actionEnnemi.reussi())
      return;

    boolean actionEnnemieOffensive = estOffensif(actionEnnemi.type());

    if (actionEnnemieOffensive && actionJoueur.reussi()
        && (actionJoueur.type() == TypeCapacite.ESQUIVE
            || actionJoueur.type() == TypeCapacite.CONTRE)) {
      if (actionJoueur.type() == TypeCapacite.CONTRE) {
        combat.setVieEnnemiActuelle(
            Math.max(0, combat.getVieEnnemiActuelle() - actionJoueur.valeur()));
      }
      return;
    }

    if (actionEnnemi.type() == TypeCapacite.ATTAQUE) {
      progression.setVieActuelle(
          Math.max(0, progression.getVieActuelle() - actionEnnemi.valeur()));
    } else if (actionEnnemi.type() == TypeCapacite.SOIN) {
      combat.setVieEnnemiActuelle(
          Math.min(combat.getEnnemi().getVieMax(),
              combat.getVieEnnemiActuelle() + actionEnnemi.valeur()));
    } else if (actionEnnemi.type() == TypeCapacite.PARALYSIE) {
      combat.setToursParalysieJoueur(
          Math.max(combat.getToursParalysieJoueur(), actionEnnemi.duree()));
    }
  }

  private RecompenseCombatResponse verifierFinCombat(Combat combat, ProgressionJoueur progression) {
    if (combat.getVieEnnemiActuelle() <= 0) {
      return appliquerVictoire(combat, progression);
    }
    if (progression.getVieActuelle() <= 0) {
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

  private void verifierAucunCombatEnCours(ProgressionJoueur progression) {
    combatRepository.findByProgressionJoueurIdAndStatut(progression.getId(), StatutCombat.EN_COURS)
        .ifPresent(c -> {
          throw new IllegalStateException("Un combat est déjà en cours");
        });
  }

  private void verifierEnduranceSuffisante(ProgressionJoueur progression) {
    if (progression.getEnduranceActuelle() <= 0) {
      throw new IllegalStateException("Endurance insuffisante pour lancer un combat");
    }
  }

  private void verifierCoutEndurance(ProgressionJoueur progression, Capacite capacite) {
    if (progression.getEnduranceActuelle() < capacite.getCoutEndurance()) {
      throw new IllegalStateException(
          "Endurance insuffisante pour utiliser cette capacité (coût : "
              + capacite.getCoutEndurance() + ")");
    }
  }

  private void verifierNonParalyse(Combat combat) {
    if (combat.getToursParalysieJoueur() > 0) {
      throw new IllegalStateException(
          "Vous êtes paralysé et ne pouvez pas agir ce tour ("
              + combat.getToursParalysieJoueur() + " tour(s) restant(s))");
    }
  }

  private void verifierAccesZone(ProgressionJoueur progression, Zone zone) {
    if (progression.getNiveau() < zone.getNiveauRequis()) {
      throw new IllegalStateException(
          "Niveau insuffisant pour accéder à cette zone (requis : " + zone.getNiveauRequis() + ")");
    }
  }

  private Ennemi choisirEnnemi(ProgressionJoueur progression, Zone zone) {
    if (bossDejaVaincu(progression, zone)) {
      throw new IllegalStateException(
          "Le boss de cette zone a déjà été vaincu. Passez à la zone suivante.");
    }
    if (bossEstDisponible(progression, zone)) {
      return ennemiRepository.findByZoneIdAndBossTrue(zone.getId())
          .orElseThrow(() -> new RessourceIntrouvableException("Boss introuvable dans cette zone"));
    }
    return choisirEnnemiAleatoire(zone);
  }

  private boolean bossEstDisponible(ProgressionJoueur progression, Zone zone) {
    return progression.getNiveau() >= zone.getNiveauRequis()
        && ennemiRepository.findByZoneIdAndBossTrue(zone.getId()).isPresent();
  }

  private boolean bossDejaVaincu(ProgressionJoueur progression, Zone zone) {
    return ennemiRepository.findByZoneIdAndBossTrue(zone.getId())
        .map(boss -> combatRepository.existsByProgressionJoueurIdAndEnnemiIdAndStatut(
            progression.getId(), boss.getId(), StatutCombat.VICTOIRE))
        .orElse(false);
  }

  private Ennemi choisirEnnemiAleatoire(Zone zone) {
    List<Ennemi> liste = ennemiRepository.findByZoneIdAndBossFalse(zone.getId());
    if (liste.isEmpty()) {
      throw new RessourceIntrouvableException("Aucun ennemi disponible dans cette zone");
    }
    return liste.get(RANDOM.nextInt(liste.size()));
  }

  private boolean capaciteAtteintSaCible(Capacite capacite) {
    int precision = capacite.getPrecision() != null ? capacite.getPrecision() : 100;
    if (precision >= 100)
      return true;
    return (RANDOM.nextInt(100) + 1) <= precision;
  }

  private boolean estOffensif(TypeCapacite type) {
    return type == TypeCapacite.ATTAQUE || type == TypeCapacite.PARALYSIE;
  }

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
    while (progression.getExperience() >= experienceRequise(progression.getNiveau())) {
      monterDeNiveau(progression);
    }
  }

  private void monterDeNiveau(ProgressionJoueur progression) {
    progression.setNiveau(progression.getNiveau() + 1);
    progression.setPuissance(progression.getPuissance() + BONUS_PUISSANCE_PAR_NIVEAU);
    progression.setVieMax(progression.getVieMax() + BONUS_VIE_MAX_PAR_NIVEAU);
    progression.setVieActuelle(progression.getVieMax());
    progression.setEnduranceMax(progression.getEnduranceMax() + BONUS_ENDURANCE_MAX_PAR_NIVEAU);
    progression.setEnduranceActuelle(progression.getEnduranceMax());
  }

  private void consommerEndurance(ProgressionJoueur progression, Capacite capacite) {
    progression.setEnduranceActuelle(
        progression.getEnduranceActuelle() - capacite.getCoutEndurance());
  }

  private int bonusPuissance(int puissance) {
    return (int) Math.sqrt(puissance);
  }

  private int valeurAleatoireEntre(int min, int max) {
    if (min > max)
      throw new IllegalArgumentException("min ne peut pas être supérieur à max");
    return RANDOM.nextInt(min, max + 1);
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
    boolean victoireBoss = combat.getStatut() == StatutCombat.VICTOIRE && combat.getEnnemi().isBoss();
    boolean factionsDebloquees = victoireBoss && combat.getEnnemi().getId() == ID_BOSS_HIGUMA;

    return new CombatResponse(
        combat.getId(),
        combat.getEnnemi().getNom(),
        combat.getVieEnnemiActuelle(),
        combat.getProgressionJoueur().getVieActuelle(),
        combat.getProgressionJoueur().getEnduranceActuelle(),
        victoireBoss,
        factionsDebloquees,
        combat.getStatut(),
        recompense);
  }
}