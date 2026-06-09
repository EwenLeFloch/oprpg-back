package com.onepiecerpg.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onepiecerpg.api.entity.Ile;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.IleRepository;

@Service
public class IleService {

  private final IleRepository ileRepository;

  public IleService(IleRepository ileRepository) {
    this.ileRepository = ileRepository;
  }

  public List<Ile> recupererToutesLesIles() {
    return ileRepository.findAll();
  }

  public Ile recupererIleParId(Long ileId) {
    return ileRepository.findById(ileId)
        .orElseThrow(() -> new RessourceIntrouvableException("Île introuvable"));
  }

  public Ile recupererIleParNom(String nom) {
    return ileRepository.findByNom(nom)
        .orElseThrow(() -> new RessourceIntrouvableException("Île introuvable"));
  }
}