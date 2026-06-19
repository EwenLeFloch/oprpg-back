package com.onepiecerpg.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.PersonnageResponse;
import com.onepiecerpg.api.service.PersonnageService;

@RestController
@RequestMapping("/api/personnages")
public class PersonnageController {

  private final PersonnageService personnageService;

  public PersonnageController(PersonnageService personnageService) {
    this.personnageService = personnageService;
  }

  @GetMapping("/{nom}")
  public ResponseEntity<PersonnageResponse> recupererParNom(@PathVariable String nom) {
    return ResponseEntity.ok(personnageService.recupererParNom(nom));
  }
}