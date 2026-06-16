package com.onepiecerpg.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConnexionRequest {

  @NotBlank(message = "L'identifiant est obligatoire")
  private String identifiant;

  @NotBlank(message = "Le mot de passe est obligatoire")
  private String motDePasse;
}