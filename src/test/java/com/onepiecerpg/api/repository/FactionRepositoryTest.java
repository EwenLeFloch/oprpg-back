package com.onepiecerpg.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.onepiecerpg.api.entity.Faction;

@DataJpaTest
class FactionRepositoryTest {

  @Autowired
  private FactionRepository factionRepository;

  @Test
  void shouldFindByNom() {
    Faction faction = new Faction();
    faction.setNom("Pirate");
    faction.setDescription("Faction pirate");

    factionRepository.save(faction);

    Optional<Faction> result = factionRepository.findByNom("Pirate");

    assertThat(result).isPresent();
    assertThat(result.get().getDescription()).isEqualTo("Faction pirate");
  }
}