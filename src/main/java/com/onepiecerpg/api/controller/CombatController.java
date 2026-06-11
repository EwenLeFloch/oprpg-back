package com.onepiecerpg.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.CombatResponse;
import com.onepiecerpg.api.service.CombatService;

@RestController
@RequestMapping("/api/combats")
public class CombatController {

  private final CombatService combatService;

  public CombatController(CombatService combatService) {
    this.combatService = combatService;
  }

  @PostMapping("/ennemis/{ennemiId}")
  public ResponseEntity<CombatResponse> demarrerCombat(@PathVariable Long ennemiId) {
    return ResponseEntity.ok(combatService.demarrerCombat(ennemiId));
  }

  @GetMapping("/en-cours")
  public ResponseEntity<CombatResponse> recupererCombatEnCours() {
    return ResponseEntity.ok(combatService.recupererCombatEnCours());
  }

  @PostMapping("/capacites/{capaciteId}")
  public ResponseEntity<CombatResponse> utiliserCapacite(@PathVariable Long capaciteId) {
    return ResponseEntity.ok(combatService.utiliserCapacite(capaciteId));
  }

  @PostMapping("/fuite")
  public ResponseEntity<CombatResponse> fuirCombat() {
    return ResponseEntity.ok(combatService.fuirCombat());
  }
}