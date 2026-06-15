package com.onepiecerpg.api.dto;

import com.onepiecerpg.api.entity.StatutCombat;

public record CombatResponse(
    Long combatId,
    String ennemi,
    int vieEnnemiActuelle,
    int vieJoueurActuelle,
    int enduranceActuelle,
    boolean bossVaincu,
    boolean factionsDebloquees,
    StatutCombat statut,
    RecompenseCombatResponse recompense) {
}