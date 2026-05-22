package com.onepiecerpg.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InscriptionRequest {
  
  @NotBlank(message = "Le nom d'utilisateur est obligatoire")
  private String pseudo;

  @Email(message = "L'email doit être valide")
  @NotBlank(message = "L'email est obligatoire")
  private String email;

  @Pattern(
      regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
      message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre"
    )
  @Size(max = 72, message = "Le mot de passe ne doit pas dépasser 72 caractères")
  @NotBlank(message = "Le mot de passe est obligatoire")
  private String motDePasse;
}
