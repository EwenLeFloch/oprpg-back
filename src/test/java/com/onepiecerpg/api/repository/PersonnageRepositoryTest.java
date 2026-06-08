package com.onepiecerpg.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.onepiecerpg.api.entity.Personnage;

@DataJpaTest
class PersonnageRepositoryTest {

    @Autowired
    private PersonnageRepository personnageRepository;

    @Test
    void shouldFindByNom() {
        Personnage personnage = new Personnage();
        personnage.setNom("Luffy");
        personnage.setDescription("Personnage de départ");
        personnage.setJouable(true);

        personnageRepository.save(personnage);

        Optional<Personnage> result = personnageRepository.findByNom("Luffy");

        assertThat(result).isPresent();
        assertThat(result.get().getDescription()).isEqualTo("Personnage de départ");
    }
}