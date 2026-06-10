package com.onepiecerpg.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.ConnexionRequest;
import com.onepiecerpg.api.dto.ConnexionResponse;
import com.onepiecerpg.api.dto.InscriptionRequest;
import com.onepiecerpg.api.dto.UtilisateurResponseDto;
import com.onepiecerpg.api.service.UtilisateurService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UtilisateurService utilisateurService;

  public AuthController(UtilisateurService utilisateurService) {
    this.utilisateurService = utilisateurService;
  }

  @PostMapping("/inscription")
  @Operation(summary = "Inscrire un nouvel utilisateur")
  @ApiResponse(responseCode = "201", description = "Utilisateur créé avec succès")
  @ApiResponse(responseCode = "400", description = "Données invalides")
  public ResponseEntity<UtilisateurResponseDto> inscription(
      @Valid @RequestBody InscriptionRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(utilisateurService.creerUtilisateur(request));
  }

  @PostMapping("/connexion")
  @Operation(summary = "Connecter un utilisateur")
  @ApiResponse(responseCode = "200", description = "Connexion réussie")
  @ApiResponse(responseCode = "400", description = "Données invalides")
  @ApiResponse(responseCode = "401", description = "Identifiants incorrects")
  public ResponseEntity<ConnexionResponse> connexion(
      @Valid @RequestBody ConnexionRequest request) {
    return ResponseEntity.ok(utilisateurService.connecterUtilisateur(request));
  }
}