package com.onepiecerpg.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onepiecerpg.api.entity.Faction;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.FactionRepository;

@Service
public class FactionService {
    private final FactionRepository factionRepository;

    public FactionService(FactionRepository factionRepository) {
        this.factionRepository = factionRepository;
    }

    public List<Faction> recupererToutesLesFactions() {
        return factionRepository.findAll();
    }

    public Faction recupererFactionParId(Long id) {
        return factionRepository.findById(id)
            .orElseThrow(() -> new RessourceIntrouvableException("Faction non trouvée"));
    }

    public Faction recupererFactionParNom(String nom) {
        return factionRepository.findByNom(nom)
            .orElseThrow(() -> new RessourceIntrouvableException("Faction non trouvée"));
    }
}
