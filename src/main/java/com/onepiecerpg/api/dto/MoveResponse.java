package com.onepiecerpg.api.dto;

import com.onepiecerpg.api.entity.Move;
import com.onepiecerpg.api.entity.TypeMove;

public record MoveResponse(
        Long id,
        String nom,
        String description,
        TypeMove typeMove,
        int valeurMin,
        int valeurMax,
        int duree,
        Integer precision,
        int coutEndurance
) {
    public static MoveResponse from(Move move) {
        return new MoveResponse(
                move.getId(),
                move.getNom(),
                move.getDescription(),
                move.getTypeMove(),
                move.getValeurMin(),
                move.getValeurMax(),
                move.getDuree(),
                move.getPrecision(),
                move.getCoutEndurance()
        );
    }
}