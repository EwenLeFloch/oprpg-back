package com.onepiecerpg.api.dto;

import com.onepiecerpg.api.entity.Ile;

public record IleResponse(
    Long id,
    String nom,
    String imagePath,
    String description,
    int niveauRequis) {
  public static IleResponse from(Ile ile) {
    return new IleResponse(
        ile.getId(),
        ile.getNom(),
        ile.getImagePath(),
        ile.getDescription(),
        ile.getNiveauRequis());
  }
}