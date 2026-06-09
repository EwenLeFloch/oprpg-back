package com.onepiecerpg.api.dto;

public record UtilisateurResponseDto(
    Long id,
    String pseudo,
    String email,
    String role) {
}
