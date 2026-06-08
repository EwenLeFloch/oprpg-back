package com.onepiecerpg.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.onepiecerpg.api.entity.Ennemi;
import com.onepiecerpg.api.entity.Ile;
import com.onepiecerpg.api.entity.Zone;

@DataJpaTest
class EnnemiRepositoryTest {

    @Autowired
    private EnnemiRepository ennemiRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private IleRepository ileRepository;

    @Test
    void shouldFindByNom() {
        Zone zone = zoneRepository.save(zone(ileRepository.save(ile())));

        Ennemi ennemi = ennemi("Bandit", false, zone);
        ennemiRepository.save(ennemi);

        Optional<Ennemi> result = ennemiRepository.findByNom("Bandit");

        assertThat(result).isPresent();
        assertThat(result.get().isBoss()).isFalse();
    }

    @Test
    void shouldFindByZoneId() {
        Zone zone = zoneRepository.save(zone(ileRepository.save(ile())));

        ennemiRepository.save(ennemi("Bandit", false, zone));
        ennemiRepository.save(ennemi("Higuma", true, zone));

        List<Ennemi> result = ennemiRepository.findByZoneId(zone.getId());

        assertThat(result).hasSize(2);
    }

    @Test
    void shouldFindByZoneIdAndBossFalse() {
        Zone zone = zoneRepository.save(zone(ileRepository.save(ile())));

        ennemiRepository.save(ennemi("Bandit", false, zone));
        ennemiRepository.save(ennemi("Higuma", true, zone));

        List<Ennemi> result = ennemiRepository.findByZoneIdAndBossFalse(zone.getId());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNom()).isEqualTo("Bandit");
    }

    private Ile ile() {
        Ile ile = new Ile();
        ile.setNom("Dawn Island");
        ile.setImage_path("/images/dawn-island.png");
        ile.setDescription("Île de départ");
        ile.setNiveauRequis(1);
        return ile;
    }

    private Zone zone(Ile ile) {
        Zone zone = new Zone();
        zone.setNom("Village Fuschia");
        zone.setNiveauRequis(1);
        zone.setIle(ile);
        return zone;
    }

    private Ennemi ennemi(String nom, boolean boss, Zone zone) {
        Ennemi ennemi = new Ennemi();
        ennemi.setNom(nom);
        ennemi.setVieMax(20);
        ennemi.setPuissance(3);
        ennemi.setExperienceMin(5);
        ennemi.setExperienceMax(10);
        ennemi.setBoss(boss);
        ennemi.setZone(zone);
        return ennemi;
    }
}