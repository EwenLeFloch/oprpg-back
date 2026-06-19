package com.onepiecerpg.api.dto;

import com.onepiecerpg.api.entity.Personnage;

public record PersonnageResponse(
    Long id,
    String nom,
    String nomImage,
    String description) {

  public static PersonnageResponse from(Personnage personnage) {
    return new PersonnageResponse(
        personnage.getId(),
        personnage.getNom(),
        personnage.getNomImage(),
        personnage.getDescription());
  }
}