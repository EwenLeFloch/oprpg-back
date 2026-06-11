package com.onepiecerpg.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.CapaciteResponse;
import com.onepiecerpg.api.entity.TypeCapacite;
import com.onepiecerpg.api.service.CapaciteService;

@RestController
@RequestMapping("/api/capacites")
public class CapaciteController {

  private final CapaciteService capaciteService;

  public CapaciteController(CapaciteService capaciteService) {
    this.capaciteService = capaciteService;
  }

  @GetMapping
  public ResponseEntity<List<CapaciteResponse>> recupererTousLesCapacites() {
    return ResponseEntity.ok(
        capaciteService.recupererTousLesCapacites()
            .stream()
            .map(CapaciteResponse::from)
            .toList());
  }

  @GetMapping("/personnage")
  public ResponseEntity<List<CapaciteResponse>> recupererCapacitesPersonnageConnecte() {
    return ResponseEntity.ok(
        capaciteService.recupererCapacitesPersonnageConnecte()
            .stream()
            .map(CapaciteResponse::from)
            .toList());
  }

  @GetMapping("/{capaciteId}")
  public ResponseEntity<CapaciteResponse> recupererCapaciteParId(@PathVariable Long capaciteId) {
    return ResponseEntity.ok(CapaciteResponse.from(capaciteService.recupererCapaciteParId(capaciteId)));
  }

  @GetMapping("/type/{typeCapacite}")
  public ResponseEntity<List<CapaciteResponse>> recupererCapacitesParType(@PathVariable TypeCapacite typeCapacite) {
    return ResponseEntity.ok(
        capaciteService.recupererCapacitesParType(typeCapacite)
            .stream()
            .map(CapaciteResponse::from)
            .toList());
  }
}