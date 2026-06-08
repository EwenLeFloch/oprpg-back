package com.onepiecerpg.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.onepiecerpg.api.entity.Ile;
import com.onepiecerpg.api.entity.Zone;

@DataJpaTest
class ZoneRepositoryTest {

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private IleRepository ileRepository;

    @Test
    void shouldFindByNom() {
        Ile ile = ileRepository.save(ile());

        Zone zone = zone("Village Fuschia", ile);
        zoneRepository.save(zone);

        Optional<Zone> result = zoneRepository.findByNom("Village Fuschia");

        assertThat(result).isPresent();
        assertThat(result.get().getIle().getNom()).isEqualTo("Dawn Island");
    }

    @Test
    void shouldFindByIleId() {
        Ile ile = ileRepository.save(ile());

        zoneRepository.save(zone("Village Fuschia", ile));
        zoneRepository.save(zone("Forêt de Fuschia", ile));

        List<Zone> result = zoneRepository.findByIleId(ile.getId());

        assertThat(result).hasSize(2);
    }

    private Ile ile() {
        Ile ile = new Ile();
        ile.setNom("Dawn Island");
        ile.setImage_path("/images/dawn-island.png");
        ile.setDescription("Île de départ");
        ile.setNiveauRequis(1);
        return ile;
    }

    private Zone zone(String nom, Ile ile) {
        Zone zone = new Zone();
        zone.setNom(nom);
        zone.setNiveauRequis(1);
        zone.setIle(ile);
        return zone;
    }
}