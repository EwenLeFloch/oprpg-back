package com.onepiecerpg.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onepiecerpg.api.entity.Zone;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.IleRepository;
import com.onepiecerpg.api.repository.ZoneRepository;

@Service
public class ZoneService {

  private final ZoneRepository zoneRepository;
  private final IleRepository ileRepository;

  public ZoneService(
      ZoneRepository zoneRepository,
      IleRepository ileRepository) {
    this.zoneRepository = zoneRepository;
    this.ileRepository = ileRepository;
  }

  public List<Zone> recupererToutesLesZones() {
    return zoneRepository.findAll();
  }

  public Zone recupererZoneParId(Long zoneId) {
    return zoneRepository.findById(zoneId)
        .orElseThrow(() -> new RessourceIntrouvableException("Zone introuvable"));
  }

  public Zone recupererZoneParNom(String nom) {
    return zoneRepository.findByNom(nom)
        .orElseThrow(() -> new RessourceIntrouvableException("Zone introuvable"));
  }

  public List<Zone> recupererZonesParIle(Long ileId) {
    if (!ileRepository.existsById(ileId)) {
      throw new RessourceIntrouvableException("Île introuvable");
    }

    return zoneRepository.findByIleId(ileId);
  }
}