package com.onepiecerpg.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.ConnexionRequest;
import com.onepiecerpg.api.dto.ConnexionResponse;
import com.onepiecerpg.api.dto.InscriptionRequest;
import com.onepiecerpg.api.dto.UtilisateurResponseDto;
import com.onepiecerpg.api.service.UtilisateurService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UtilisateurService utilisateurService;

  public AuthController(UtilisateurService utilisateurService) {
    this.utilisateurService = utilisateurService;
  }

  @PostMapping("/inscription")
  public ResponseEntity<UtilisateurResponseDto> inscription(
      @Valid @RequestBody InscriptionRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(utilisateurService.creerUtilisateur(request));
  }

  @PostMapping("/connexion")
  public ResponseEntity<ConnexionResponse> connexion(
      @Valid @RequestBody ConnexionRequest request) {
    return ResponseEntity.ok(utilisateurService.connecterUtilisateur(request));
  }
}