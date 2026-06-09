package com.onepiecerpg.api.service;

import java.security.SecureRandom;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.onepiecerpg.api.dto.CombatResponse;
import com.onepiecerpg.api.entity.Combat;
import com.onepiecerpg.api.entity.Ennemi;
import com.onepiecerpg.api.entity.Move;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.StatutCombat;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.CombatRepository;
import com.onepiecerpg.api.repository.EnnemiRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;

@Service
public class CombatService {

  private static final int BONUS_VIE_MAX_PAR_NIVEAU = 2;
  private static final int BONUS_ENDURANCE_MAX_PAR_NIVEAU = 2;
  private static final int BONUS_PUISSANCE_PAR_NIVEAU = 1;

  private final CombatRepository combatRepository;
  private final EnnemiRepository ennemiRepository;
  private final ProgressionJoueurRepository progressionJoueurRepository;
  private final UtilisateurRepository utilisateurRepository;

  public CombatService(
      CombatRepository combatRepository,
      EnnemiRepository ennemiRepository,
      ProgressionJoueurRepository progressionJoueurRepository,
      UtilisateurRepository utilisateurRepository) {
    this.combatRepository = combatRepository;
    this.ennemiRepository = ennemiRepository;
    this.progressionJoueurRepository = progressionJoueurRepository;
    this.utilisateurRepository = utilisateurRepository;
  }

  public CombatResponse demarrerCombat(Long ennemiId) {
    ProgressionJoueur progression = recupererProgressionConnectee();
    verifierAucunCombatEnCours(progression);

    Ennemi ennemi = recupererEnnemi(ennemiId);

    Combat combat = new Combat();
    combat.setProgressionJoueur(progression);
    combat.setEnnemi(ennemi);
    combat.setVieEnnemiActuelle(ennemi.getVieMax());

    return convertir(combatRepository.save(combat));
  }

  public CombatResponse recupererCombatEnCours() {
    return convertir(recupererCombatEnCoursConnecte());
  }

  public CombatResponse utiliserMove(Long moveId) {
    ProgressionJoueur progression = recupererProgressionConnectee();
    Combat combat = recupererCombatEnCours(progression);
    Move move = recupererMoveJoueur(progression, moveId);

    appliquerMove(combat, progression, move);
    verifierFinCombat(combat, progression);

    combatRepository.save(combat);
    progressionJoueurRepository.save(progression);

    return convertir(combat);
  }

  public CombatResponse fuirCombat() {
    Combat combat = recupererCombatEnCoursConnecte();
    combat.setStatut(StatutCombat.FUITE);

    return convertir(combatRepository.save(combat));
  }

  private void appliquerMove(Combat combat, ProgressionJoueur progression, Move move) {
    switch (move.getTypeMove()) {
      case ATTAQUE -> appliquerAttaque(combat, progression, move);
      case SOIN -> appliquerSoin(progression, move);
      default -> throw new IllegalArgumentException("Type de move non géré pour le moment");
    }
  }

  private void appliquerAttaque(Combat combat, ProgressionJoueur progression, Move move) {
    int degats = calculerDegats(progression, move);
    int nouvelleVieEnnemi = combat.getVieEnnemiActuelle() - degats;

    combat.setVieEnnemiActuelle(Math.max(0, nouvelleVieEnnemi));
  }

  private void appliquerSoin(ProgressionJoueur progression, Move move) {
    int soin = calculerSoin(move);
    int nouvelleVieJoueur = progression.getVieActuelle() + soin;

    progression.setVieActuelle(Math.min(progression.getVieMax(), nouvelleVieJoueur));
  }

  private void verifierFinCombat(Combat combat, ProgressionJoueur progression) {
    if (ennemiEstVaincu(combat)) {
      appliquerVictoire(combat, progression);
      return;
    }

    appliquerTourEnnemi(combat, progression);

    if (joueurEstVaincu(progression)) {
      combat.setStatut(StatutCombat.DEFAITE);
    }
  }

  private void appliquerVictoire(Combat combat, ProgressionJoueur progression) {
    combat.setStatut(StatutCombat.VICTOIRE);

    int experienceGagnee = calculerExperienceGagnee(combat.getEnnemi());
    appliquerExperience(progression, experienceGagnee);
  }

  private void appliquerTourEnnemi(Combat combat, ProgressionJoueur progression) {
    int degatsEnnemi = combat.getEnnemi().getPuissance();
    int nouvelleVieJoueur = progression.getVieActuelle() - degatsEnnemi;

    progression.setVieActuelle(Math.max(0, nouvelleVieJoueur));
  }

  private boolean ennemiEstVaincu(Combat combat) {
    return combat.getVieEnnemiActuelle() <= 0;
  }

  private boolean joueurEstVaincu(ProgressionJoueur progression) {
    return progression.getVieActuelle() <= 0;
  }

  private int calculerDegats(ProgressionJoueur progression, Move move) {
    int base = valeurAleatoireEntre(move.getValeurMin(), move.getValeurMax());
    return base + bonusPuissance(progression.getPuissance());
  }

  private int calculerSoin(Move move) {
    return valeurAleatoireEntre(move.getValeurMin(), move.getValeurMax());
  }

  private int bonusPuissance(int puissance) {
    return (int) Math.sqrt(puissance);
  }

  private int calculerExperienceGagnee(Ennemi ennemi) {
    return valeurAleatoireEntre(ennemi.getExperienceMin(), ennemi.getExperienceMax());
  }

  private void appliquerExperience(ProgressionJoueur progression, int experienceGagnee) {
    progression.setExperience(progression.getExperience() + experienceGagnee);

    while (peutMonterDeNiveau(progression)) {
      monterDeNiveau(progression);
    }
  }

  private boolean peutMonterDeNiveau(ProgressionJoueur progression) {
    int niveauSuivant = progression.getNiveau() + 1;
    return progression.getExperience() >= experienceRequise(niveauSuivant);
  }

  private void monterDeNiveau(ProgressionJoueur progression) {
    progression.setNiveau(progression.getNiveau() + 1);
    progression.setPuissance(progression.getPuissance() + BONUS_PUISSANCE_PAR_NIVEAU);
    progression.setVieMax(progression.getVieMax() + BONUS_VIE_MAX_PAR_NIVEAU);
    progression.setVieActuelle(progression.getVieMax());
    progression.setEnduranceMax(progression.getEnduranceMax() + BONUS_ENDURANCE_MAX_PAR_NIVEAU);
    progression.setEnduranceActuelle(progression.getEnduranceMax());
  }

  private int experienceRequise(int niveau) {
    int niveauEffectif = niveau - 1;
    return 20 * niveauEffectif * niveauEffectif + 10 * niveauEffectif;
  }

  private static final SecureRandom RANDOM = new SecureRandom();

  private int valeurAleatoireEntre(int min, int max) {
    if (min > max) {
      throw new IllegalArgumentException("La valeur minimale ne peut pas être supérieure à la valeur maximale");
    }
    return RANDOM.nextInt(min, max + 1);
  }

  private void verifierAucunCombatEnCours(ProgressionJoueur progression) {
    combatRepository.findByProgressionJoueurIdAndStatut(
        progression.getId(),
        StatutCombat.EN_COURS).ifPresent(combat -> {
          throw new IllegalStateException("Un combat est déjà en cours");
        });
  }

  private Combat recupererCombatEnCoursConnecte() {
    ProgressionJoueur progression = recupererProgressionConnectee();
    return recupererCombatEnCours(progression);
  }

  private Combat recupererCombatEnCours(ProgressionJoueur progression) {
    return combatRepository.findByProgressionJoueurIdAndStatut(
        progression.getId(),
        StatutCombat.EN_COURS).orElseThrow(() -> new RessourceIntrouvableException("Aucun combat en cours"));
  }

  private Ennemi recupererEnnemi(Long ennemiId) {
    return ennemiRepository.findById(ennemiId)
        .orElseThrow(() -> new RessourceIntrouvableException("Ennemi introuvable"));
  }

  private Move recupererMoveJoueur(ProgressionJoueur progression, Long moveId) {
    return progression.getPersonnage()
        .getMoves()
        .stream()
        .filter(move -> move.getId().equals(moveId))
        .findFirst()
        .orElseThrow(() -> new RessourceIntrouvableException("Move introuvable"));
  }

  private ProgressionJoueur recupererProgressionConnectee() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
        .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur connecté introuvable"));

    return progressionJoueurRepository.findByUtilisateur(utilisateur)
        .orElseThrow(() -> new RessourceIntrouvableException("Progression du joueur non trouvée"));
  }

  private CombatResponse convertir(Combat combat) {
    return new CombatResponse(
        combat.getId(),
        combat.getEnnemi().getNom(),
        combat.getVieEnnemiActuelle(),
        combat.getProgressionJoueur().getVieActuelle(),
        combat.getStatut());
  }
}