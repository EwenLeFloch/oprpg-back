package com.onepiecerpg.api.dto;

import com.onepiecerpg.api.entity.Capacite;
import com.onepiecerpg.api.entity.TypeCapacite;

public record CapaciteResponse(
    Long id,
    String nom,
    String description,
    TypeCapacite typeCapacite,
    int valeurMin,
    int valeurMax,
    int duree,
    Integer precision,
    int coutEndurance) {
  public static CapaciteResponse from(Capacite capacite) {
    return new CapaciteResponse(
        capacite.getId(),
        capacite.getNom(),
        capacite.getDescription(),
        capacite.getTypeCapacite(),
        capacite.getValeurMin(),
        capacite.getValeurMax(),
        capacite.getDuree(),
        capacite.getPrecision(),
        capacite.getCoutEndurance());
  }
}