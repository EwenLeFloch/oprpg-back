package com.onepiecerpg.api.dto;

import java.util.List;

import com.onepiecerpg.api.entity.EtatCombat;
import com.onepiecerpg.api.entity.StatutCombat;

public record CombatResponse(
    Long combatId,
    Long ennemiId,
    String ennemi,
    int vieEnnemiActuelle,
    int vieJoueurActuelle,
    int enduranceActuelle,
    EtatCombat etatJoueur,
    EtatCombat etatEnnemi,
    boolean bossVaincu,
    boolean factionsDebloquees,
    StatutCombat statut,
    List<String> historique,
    RecompenseCombatResponse recompense) {
}