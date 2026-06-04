package com.onepiecerpg.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onepiecerpg.api.entity.Ile;
import com.onepiecerpg.api.entity.Zone;
import com.onepiecerpg.api.repository.IleRepository;
import com.onepiecerpg.api.repository.ZoneRepository;

@Service
public class MondeService {
    
    private final IleRepository ileRepository;
    private final ZoneRepository zoneRepository;

    public MondeService(IleRepository ileRepository, ZoneRepository zoneRepository) {
        this.ileRepository = ileRepository;
        this.zoneRepository = zoneRepository;
    }

    public List<Ile> recupererToutesLesIles() {
        return ileRepository.findAll();
    }
    
    public Ile recupererIleParId(Long ileId) {
        return ileRepository.findById(ileId)
                .orElseThrow(() -> new RuntimeException("Île non trouvée"));
    }

    public List<Zone> recupererZonesParIle(Long ileId) {
        if (!ileRepository.existsById(ileId)) {
            throw new RuntimeException("Île non trouvée");
        }

        return zoneRepository.findByIleId(ileId);
    }

    public Zone recupererZoneParId(Long zoneId) {
        return zoneRepository.findById(zoneId)
                .orElseThrow(() -> new RuntimeException("Zone non trouvée"));
    }

    public boolean joueurPeutAccederZone(int niveauJoueur, Zone zone) {
        return niveauJoueur >= zone.getNiveauRequis();
    }

    public boolean joueurPeutAccederIle(int niveauJoueur, Ile ile) {
        return niveauJoueur >= ile.getNiveauRequis();
    }
}
