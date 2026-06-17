package com.onepiecerpg.api.dto;

import com.onepiecerpg.api.entity.Ile;

public record IleResponse(
    Long id,
    String nom,
    String nomImage,
    String description,
    int niveauRequis,
    int positionX,
    int positionY) {

  public static IleResponse from(Ile ile) {
    return new IleResponse(
        ile.getId(),
        ile.getNom(),
        ile.getNomImage(),
        ile.getDescription(),
        ile.getNiveauRequis(),
        ile.getPositionX(),
        ile.getPositionY());
  }
}