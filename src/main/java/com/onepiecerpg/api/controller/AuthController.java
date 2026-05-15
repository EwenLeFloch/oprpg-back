package com.onepiecerpg.api.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

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
  @ResponseStatus(HttpStatus.CREATED)
  public Utilisateur inscription(
    @Valid @RequestBody InscriptionRequest request
  ) {
    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setUsername(request.getUsername());
    utilisateur.setMail(request.getMail());
    utilisateur.setPassword(request.getPassword());

    return utilisateurService.creerUtilisateur(utilisateur);
  }
}
