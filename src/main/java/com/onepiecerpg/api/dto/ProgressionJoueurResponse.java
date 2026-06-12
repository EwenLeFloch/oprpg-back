package com.onepiecerpg.api.dto;

public record ProgressionJoueurResponse(
    Long id,

    int niveau,
    int experience,

    int enduranceMax,
    int enduranceActuelle,

    int puissance,

    int vieMax,
    int vieActuelle,

    int berries,
    Long prime,

    String personnage,
    String faction,
    Long zoneId) {
}