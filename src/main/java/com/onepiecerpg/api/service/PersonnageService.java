package com.onepiecerpg.api.service;

import org.springframework.stereotype.Service;

import com.onepiecerpg.api.dto.PersonnageResponse;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.PersonnageRepository;

@Service
public class PersonnageService {

  private final PersonnageRepository personnageRepository;

  public PersonnageService(PersonnageRepository personnageRepository) {
    this.personnageRepository = personnageRepository;
  }

  public PersonnageResponse recupererParNom(String nom) {
    return personnageRepository.findByNom(nom)
        .map(PersonnageResponse::from)
        .orElseThrow(() -> new RessourceIntrouvableException("Personnage introuvable"));
  }
}