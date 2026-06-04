package com.onepiecerpg.api.service;

import java.util.concurrent.ThreadLocalRandom;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.onepiecerpg.api.dto.CombatResponse;
import com.onepiecerpg.api.entity.*;
import com.onepiecerpg.api.repository.*;

@Service
public class CombatService {

    private final CombatRepository combatRepository;
    private final EnnemiRepository ennemiRepository;
    private final ProgressionJoueurRepository progressionJoueurRepository;
    private final UtilisateurRepository utilisateurRepository;

    public CombatService(
            CombatRepository combatRepository,
            EnnemiRepository ennemiRepository,
            ProgressionJoueurRepository progressionJoueurRepository,
            UtilisateurRepository utilisateurRepository
    ) {
        this.combatRepository = combatRepository;
        this.ennemiRepository = ennemiRepository;
        this.progressionJoueurRepository = progressionJoueurRepository;
        this.utilisateurRepository = utilisateurRepository;
    }

    public CombatResponse demarrerCombat(Long ennemiId) {

        ProgressionJoueur progression = recupererProgressionConnectee();

        combatRepository.findByProgressionJoueurIdAndStatut(
                progression.getId(),
                StatutCombat.EN_COURS
        ).ifPresent(c -> {
            throw new IllegalStateException("Un combat est déjà en cours");
        });

        Ennemi ennemi = ennemiRepository.findById(ennemiId)
                .orElseThrow(() -> new RuntimeException("Ennemi introuvable"));

        Combat combat = new Combat();
        combat.setProgressionJoueur(progression);
        combat.setEnnemi(ennemi);
        combat.setVieEnnemiActuelle(ennemi.getVieMax());

        combat = combatRepository.save(combat);

        return convertir(combat);
    }

    public CombatResponse recupererCombatEnCours() {

        ProgressionJoueur progression = recupererProgressionConnectee();

        Combat combat = combatRepository.findByProgressionJoueurIdAndStatut(
                        progression.getId(),
                        StatutCombat.EN_COURS
                )
                .orElseThrow(() -> new RuntimeException("Aucun combat en cours"));

        return convertir(combat);
    }

    public CombatResponse utiliserMove(Long moveId) {

        ProgressionJoueur progression = recupererProgressionConnectee();

        Combat combat = combatRepository.findByProgressionJoueurIdAndStatut(
                        progression.getId(),
                        StatutCombat.EN_COURS
                )
                .orElseThrow(() -> new RuntimeException("Aucun combat en cours"));

        Move move = progression.getPersonnage()
                .getMoves()
                .stream()
                .filter(m -> m.getId().equals(moveId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Move introuvable"));

        appliquerMove(combat, progression, move);

        verifierFinCombat(combat, progression);

        combatRepository.save(combat);
        progressionJoueurRepository.save(progression);

        return convertir(combat);
    }

    public CombatResponse fuirCombat() {

        ProgressionJoueur progression = recupererProgressionConnectee();

        Combat combat = combatRepository.findByProgressionJoueurIdAndStatut(
                        progression.getId(),
                        StatutCombat.EN_COURS
                )
                .orElseThrow(() -> new RuntimeException("Aucun combat en cours"));

        combat.setStatut(StatutCombat.FUITE);

        combatRepository.save(combat);

        return convertir(combat);
    }

    private void appliquerMove(
            Combat combat,
            ProgressionJoueur progression,
            Move move
    ) {

        switch (move.getTypeMove()) {

            case ATTAQUE -> {
                int degats = calculerDegats(progression, move);

                combat.setVieEnnemiActuelle(
                        Math.max(
                                0,
                                combat.getVieEnnemiActuelle() - degats
                        )
                );
            }

            case SOIN -> {
                int soin = calculerSoin(move);
                progression.setVieActuelle(
                        Math.min(
                                progression.getVieMax(),
                                progression.getVieActuelle() + soin
                        )
                );
            }

            default -> throw new IllegalArgumentException(
                    "Type de move non géré pour le moment"
            );
        }
    }

    private void verifierFinCombat(
            Combat combat,
            ProgressionJoueur progression
    ) {

        if (combat.getVieEnnemiActuelle() <= 0) {

            combat.setStatut(StatutCombat.VICTOIRE);

            progression.setExperience(
                    progression.getExperience() + 10
            );

            progression.setBerries(
                    progression.getBerries() + 100
            );

            return;
        }

        progression.setVieActuelle(
                Math.max(
                        0,
                        progression.getVieActuelle()
                                - combat.getEnnemi().getPuissance()
                )
        );

        if (progression.getVieActuelle() <= 0) {
            combat.setStatut(StatutCombat.DEFAITE);
        }
    }

    private int valeurAleatoireEntre(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private int bonusPuissance(int puissance) {
        return (int) Math.floor(Math.sqrt(puissance));
    }

    private int calculerDegats(ProgressionJoueur progression, Move move) {
        int base = valeurAleatoireEntre(move.getValeurMin(), move.getValeurMax());

        return base + bonusPuissance(progression.getPuissance());
    }

    private int calculerSoin(Move move) {
        return valeurAleatoireEntre(move.getValeurMin(), move.getValeurMax());
    }

    private ProgressionJoueur recupererProgressionConnectee() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Utilisateur utilisateur = utilisateurRepository
                .findByEmail(email)
                .orElseThrow();

        return progressionJoueurRepository
                .findByUtilisateur(utilisateur)
                .orElseThrow();
    }

    private CombatResponse convertir(Combat combat) {

        return new CombatResponse(
                combat.getId(),
                combat.getEnnemi().getNom(),
                combat.getVieEnnemiActuelle(),
                combat.getProgressionJoueur().getVieActuelle(),
                combat.getStatut()
        );
    }
}