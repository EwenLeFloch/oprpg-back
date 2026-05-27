package com.onepiecerpg.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onepiecerpg.api.dto.UtilisateurResponseDto;
import com.onepiecerpg.api.service.UtilisateurService;

@RestController
@RequestMapping("/api/utilisateurs")
public class UtilisateurController {
  private final UtilisateurService utilisateurService;

  public UtilisateurController(UtilisateurService utilisateurService) {
    this.utilisateurService = utilisateurService;
  }

  @GetMapping("/me")
  public ResponseEntity<UtilisateurResponseDto> recupererUtilisateurConnecte() {
    return ResponseEntity.ok(utilisateurService.recupererUtilisateurConnecte());
  }
}
