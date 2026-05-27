package com.onepiecerpg.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.onepiecerpg.api.dto.ConnexionRequest;
import com.onepiecerpg.api.dto.ConnexionResponse;
import com.onepiecerpg.api.dto.InscriptionRequest;
import com.onepiecerpg.api.entity.Utilisateur;
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
  public ResponseEntity<Utilisateur> inscription(
    @Valid @RequestBody InscriptionRequest request
  ) {
    Utilisateur utilisateur = utilisateurService.creerUtilisateur(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(utilisateur);
  }

  @PostMapping("/connexion")
  public ResponseEntity<ConnexionResponse> connexion(
    @Valid @RequestBody ConnexionRequest request
  ) {
    return ResponseEntity.ok(utilisateurService.connecterUtilisateur(request));
  }
}
