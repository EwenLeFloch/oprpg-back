package com.onepiecerpg.api.service;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.onepiecerpg.api.dto.ProgressionJoueurResponse;
import com.onepiecerpg.api.entity.Faction;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.repository.FactionRepository;
import com.onepiecerpg.api.repository.PersonnageRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;

@Service
public class ProgressionJoueurService {
    private final ProgressionJoueurRepository progressionJoueurRepository;
    private final PersonnageRepository personnageRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final FactionRepository factionRepository;
    
    public ProgressionJoueurService(ProgressionJoueurRepository progressionJoueurRepository,
                                   PersonnageRepository personnageRepository,
                                   UtilisateurRepository utilisateurRepository,
                                   FactionRepository factionRepository) {

        this.progressionJoueurRepository = progressionJoueurRepository;
        this.personnageRepository = personnageRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.factionRepository = factionRepository;
    }

    public ProgressionJoueur creerProgressionInitiale(Utilisateur utilisateur) {
        Personnage personnage = personnageRepository.findByNom("Luffy")
                .orElseThrow(() -> new RuntimeException("Personnage de départ non trouvé"));

        ProgressionJoueur progression = new ProgressionJoueur();
        progression.setUtilisateur(utilisateur);
        progression.setPersonnage(personnage);
        progression.setVieActuelle(progression.getVieMax());
        progression.setEnduranceActuelle(progression.getEnduranceMax());
        return progressionJoueurRepository.save(progression);
    }

    public ProgressionJoueurResponse getProgressionConnectee() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        ProgressionJoueur progression = progressionJoueurRepository.findByUtilisateur(utilisateur)
                .orElseThrow(() -> new RuntimeException("Progression du joueur non trouvée"));
                
        return convertirEnResponse(progression);

    }

    public ProgressionJoueurResponse choisirFaction(Long factionId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        Utilisateur utilisateur = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        ProgressionJoueur progression = progressionJoueurRepository.findByUtilisateur(utilisateur)
                .orElseThrow(() -> new RuntimeException("Progression du joueur non trouvée"));
                
        if (progression.getFaction() != null) {
            throw new IllegalStateException("La faction a déjà été choisie");
        }

        Faction faction = factionRepository.findById(progression.getFaction().getId())
                .orElseThrow(() -> new RuntimeException("Faction non trouvée"));

        progression.setFaction(faction);
        return convertirEnResponse(progressionJoueurRepository.save(progression));        
    }

    private ProgressionJoueurResponse convertirEnResponse(ProgressionJoueur progression) {
        return new ProgressionJoueurResponse(
            progression.getId(),
            progression.getNiveau(),
            progression.getExperience(),
            progression.getEnduranceMax(),
            progression.getEnduranceActuelle(),
            progression.getPuissance(),
            progression.getVieMax(),
            progression.getVieActuelle(),
            progression.getBerries(),
            progression.getPrime(),
            progression.getPersonnage().getNom(),
            progression.getFaction() == null ? null : progression.getFaction().getNom()
        );
    }
}
