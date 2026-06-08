package com.onepiecerpg.api.dto;

import com.onepiecerpg.api.entity.Faction;

public record FactionResponse(
        Long id,
        String nom,
        String description
) {
    public static FactionResponse from(Faction faction) {
        return new FactionResponse(
                faction.getId(),
                faction.getNom(),
                faction.getDescription()
        );
    }
}