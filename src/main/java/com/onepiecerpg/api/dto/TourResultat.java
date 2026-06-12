package com.onepiecerpg.api.dto;

import com.onepiecerpg.api.entity.TypeCapacite;

public record TourResultat(
    TypeCapacite type,
    int valeur,
    int duree,
    boolean reussi) {

  public static TourResultat rate() {
    return new TourResultat(TypeCapacite.ATTAQUE, 0, 0, false);
  }
}