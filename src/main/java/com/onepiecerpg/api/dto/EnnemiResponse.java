package com.onepiecerpg.api.dto;

import com.onepiecerpg.api.entity.Ennemi;

public record EnnemiResponse(
    Long id,
    String nom,
    String nomImage,
    int vieMax,
    int puissance,
    boolean boss,
    int niveauRequis,
    Long zoneId,
    String zoneNom) {

  public static EnnemiResponse from(Ennemi ennemi) {
    return new EnnemiResponse(
        ennemi.getId(),
        ennemi.getNom(),
        ennemi.getNomImage(),
        ennemi.getVieMax(),
        ennemi.getPuissance(),
        ennemi.isBoss(),
        ennemi.getNiveauRequis(),
        ennemi.getZone().getId(),
        ennemi.getZone().getNom());
  }
}