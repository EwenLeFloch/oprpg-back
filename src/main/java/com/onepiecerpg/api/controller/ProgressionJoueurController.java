package com.onepiecerpg.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.ProgressionJoueurResponse;
import com.onepiecerpg.api.service.ProgressionJoueurService;

@RestController
@RequestMapping("/api/progression")
public class ProgressionJoueurController {

  private final ProgressionJoueurService progressionJoueurService;

  public ProgressionJoueurController(ProgressionJoueurService progressionJoueurService) {
    this.progressionJoueurService = progressionJoueurService;
  }

  @GetMapping("/me")
  public ResponseEntity<ProgressionJoueurResponse> recupererProgressionJoueur() {
    ProgressionJoueurResponse response = progressionJoueurService.getProgressionConnectee();
    return ResponseEntity.ok(response);
  }

  @PostMapping("/faction/{factionId}")
  public ResponseEntity<ProgressionJoueurResponse> choisirFaction(@PathVariable Long factionId) {
    ProgressionJoueurResponse response = progressionJoueurService.choisirFaction(factionId);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/repos")
  public ResponseEntity<ProgressionJoueurResponse> seReposer() {
    ProgressionJoueurResponse response = progressionJoueurService.seReposer();
    return ResponseEntity.ok(response);
  }
}