package com.onepiecerpg.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.FactionResponse;
import com.onepiecerpg.api.service.FactionService;

@RestController
@RequestMapping("/api/factions")
public class FactionController {

  private final FactionService factionService;

  public FactionController(FactionService factionService) {
    this.factionService = factionService;
  }

  @GetMapping
  public ResponseEntity<List<FactionResponse>> recupererToutesLesFactions() {
    return ResponseEntity.ok(
        factionService.recupererToutesLesFactions()
            .stream()
            .map(FactionResponse::from)
            .toList());
  }

  @GetMapping("/{factionId}")
  public ResponseEntity<FactionResponse> recupererFactionParId(@PathVariable Long factionId) {
    return ResponseEntity.ok(FactionResponse.from(factionService.recupererFactionParId(factionId)));
  }
}