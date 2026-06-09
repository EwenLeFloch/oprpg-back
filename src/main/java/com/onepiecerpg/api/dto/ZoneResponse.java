package com.onepiecerpg.api.dto;

import com.onepiecerpg.api.entity.Zone;

public record ZoneResponse(
    Long id,
    String nom,
    int niveauRequis,
    Long ileId,
    String ileNom) {
  public static ZoneResponse from(Zone zone) {
    return new ZoneResponse(
        zone.getId(),
        zone.getNom(),
        zone.getNiveauRequis(),
        zone.getIle().getId(),
        zone.getIle().getNom());
  }
}