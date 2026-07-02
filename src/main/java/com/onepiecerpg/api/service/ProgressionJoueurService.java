package com.onepiecerpg.api.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.onepiecerpg.api.dto.ProgressionJoueurResponse;
import com.onepiecerpg.api.entity.Faction;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.entity.Zone;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.FactionRepository;
import com.onepiecerpg.api.repository.PersonnageRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;
import com.onepiecerpg.api.repository.ZoneRepository;

@Service
public class ProgressionJoueurService {

  private static final String NOM_PERSONNAGE_DEPART = "Luffy";
  private static final String NOM_ZONE_DEPART = "Village Fuschia";

  private final ProgressionJoueurRepository progressionJoueurRepository;
  private final PersonnageRepository personnageRepository;
  private final UtilisateurRepository utilisateurRepository;
  private final FactionRepository factionRepository;
  private final ZoneRepository zoneRepository;

  public ProgressionJoueurService(
      ProgressionJoueurRepository progressionJoueurRepository,
      PersonnageRepository personnageRepository,
      UtilisateurRepository utilisateurRepository,
      FactionRepository factionRepository,
      ZoneRepository zoneRepository) {
    this.progressionJoueurRepository = progressionJoueurRepository;
    this.personnageRepository = personnageRepository;
    this.utilisateurRepository = utilisateurRepository;
    this.factionRepository = factionRepository;
    this.zoneRepository = zoneRepository;
  }

  public ProgressionJoueur creerProgressionInitiale(Utilisateur utilisateur) {
    Personnage personnage = personnageRepository.findByNom(NOM_PERSONNAGE_DEPART)
        .orElseThrow(() -> new RessourceIntrouvableException("Personnage de départ non trouvé"));

    Zone zone = zoneRepository.findByNom(NOM_ZONE_DEPART)
        .orElseThrow(() -> new RessourceIntrouvableException("Zone de départ non trouvée"));

    ProgressionJoueur progression = new ProgressionJoueur();
    progression.setUtilisateur(utilisateur);
    progression.setPersonnage(personnage);
    progression.setZone(zone);
    progression.setVieActuelle(progression.getVieMax());
    progression.setEnduranceActuelle(progression.getEnduranceMax());

    return progressionJoueurRepository.save(progression);
  }

  public ProgressionJoueurResponse getProgressionConnectee() {
    ProgressionJoueur progression = recupererProgressionConnectee();
    return convertirEnResponse(progression);
  }

  public ProgressionJoueurResponse choisirFaction(Long factionId) {
    ProgressionJoueur progression = recupererProgressionConnectee();

    if (progression.getFaction() != null) {
      throw new IllegalStateException("La faction a déjà été choisie");
    }

    Faction faction = factionRepository.findById(factionId)
        .orElseThrow(() -> new RessourceIntrouvableException("Faction non trouvée"));

    progression.setFaction(faction);

    return convertirEnResponse(progressionJoueurRepository.save(progression));
  }

  public ProgressionJoueurResponse seReposer() {
    ProgressionJoueur progression = recupererProgressionConnectee();

    progression.setEnduranceActuelle(progression.getEnduranceMax());

    return convertirEnResponse(progressionJoueurRepository.save(progression));
  }

  public ProgressionJoueurResponse boireDuLait() {
    ProgressionJoueur progression = recupererProgressionConnectee();
    progression.setVieActuelle(progression.getVieMax());
    progressionJoueurRepository.save(progression);
    return convertirEnResponse(progression);
  }

  private ProgressionJoueur recupererProgressionConnectee() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
        .orElseThrow(() -> new RessourceIntrouvableException("Utilisateur non trouvé"));

    return progressionJoueurRepository.findByUtilisateur(utilisateur)
        .orElseThrow(() -> new RessourceIntrouvableException("Progression du joueur non trouvée"));
  }

  private ProgressionJoueurResponse convertirEnResponse(ProgressionJoueur progression) {
    return new ProgressionJoueurResponse(
        progression.getId(),
        progression.getNiveau(),
        progression.getExperience(),
        ExperienceCalculator.experienceRequise(progression.getNiveau()),
        progression.getEnduranceMax(),
        progression.getEnduranceActuelle(),
        progression.getPuissance(),
        progression.getVieMax(),
        progression.getVieActuelle(),
        progression.getBerries(),
        progression.getPrime(),
        progression.getPersonnage().getNom(),
        progression.getFaction() == null ? null : progression.getFaction().getNom(),
        progression.getZone().getId());
  }
}