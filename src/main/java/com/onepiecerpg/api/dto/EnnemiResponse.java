package com.onepiecerpg.api.dto;

import com.onepiecerpg.api.entity.Ennemi;

public record EnnemiResponse(
        Long id,
        String nom,
        int vieMax,
        int puissance,
        int experienceMin,
        int experienceMax,
        boolean boss,
        Long zoneId,
        String zoneNom
) {
    public static EnnemiResponse from(Ennemi ennemi) {
        return new EnnemiResponse(
                ennemi.getId(),
                ennemi.getNom(),
                ennemi.getVieMax(),
                ennemi.getPuissance(),
                ennemi.getExperienceMin(),
                ennemi.getExperienceMax(),
                ennemi.isBoss(),
                ennemi.getZone().getId(),
                ennemi.getZone().getNom()
        );
    }
}