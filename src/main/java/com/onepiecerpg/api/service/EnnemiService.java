package com.onepiecerpg.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onepiecerpg.api.entity.Ennemi;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.EnnemiRepository;
import com.onepiecerpg.api.repository.ZoneRepository;

@Service
public class EnnemiService {

  private final EnnemiRepository ennemiRepository;
  private final ZoneRepository zoneRepository;

  public EnnemiService(
      EnnemiRepository ennemiRepository,
      ZoneRepository zoneRepository) {
    this.ennemiRepository = ennemiRepository;
    this.zoneRepository = zoneRepository;
  }

  public List<Ennemi> recupererEnnemisParZone(Long zoneId) {
    verifierZoneExiste(zoneId);
    return ennemiRepository.findByZoneId(zoneId);
  }

  public List<Ennemi> recupererEnnemisClassiquesParZone(Long zoneId) {
    verifierZoneExiste(zoneId);
    return ennemiRepository.findByZoneIdAndBossFalse(zoneId);
  }

  public Ennemi recupererEnnemiParId(Long ennemiId) {
    return ennemiRepository.findById(ennemiId)
        .orElseThrow(() -> new RessourceIntrouvableException("Ennemi introuvable"));
  }

  public Ennemi recupererEnnemiParNom(String nom) {
    return ennemiRepository.findByNom(nom)
        .orElseThrow(() -> new RessourceIntrouvableException("Ennemi introuvable"));
  }

  private void verifierZoneExiste(Long zoneId) {
    if (!zoneRepository.existsById(zoneId)) {
      throw new RessourceIntrouvableException("Zone introuvable");
    }
  }
}