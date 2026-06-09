package com.onepiecerpg.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewsRequest(
    @NotBlank(message = "Le titre est obligatoire") @Size(max = 150, message = "Le titre ne doit pas dépasser 150 caractères") String titre,

    @NotBlank(message = "Le contenu est obligatoire") @Size(max = 5000, message = "Le contenu ne doit pas dépasser 5000 caractères") String contenu) {
}