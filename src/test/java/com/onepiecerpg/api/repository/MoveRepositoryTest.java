package com.onepiecerpg.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.onepiecerpg.api.entity.Capacite;
import com.onepiecerpg.api.entity.TypeCapacite;

@DataJpaTest
class CapaciteRepositoryTest {

  @Autowired
  private CapaciteRepository capaciteRepository;

  @Test
  void shouldFindByNom() {
    Capacite capacite = capacite("Coup de poing", TypeCapacite.ATTAQUE);

    capaciteRepository.save(capacite);

    Optional<Capacite> result = capaciteRepository.findByNom("Coup de poing");

    assertThat(result).isPresent();
    assertThat(result.get().getTypeCapacite()).isEqualTo(TypeCapacite.ATTAQUE);
  }

  @Test
  void shouldFindByTypeCapacite() {
    capaciteRepository.save(capacite("Coup de poing", TypeCapacite.ATTAQUE));
    capaciteRepository.save(capacite("Lait", TypeCapacite.SOIN));

    List<Capacite> result = capaciteRepository.findByTypeCapacite(TypeCapacite.ATTAQUE);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().getNom()).isEqualTo("Coup de poing");
  }

  private Capacite capacite(String nom, TypeCapacite typeCapacite) {
    Capacite capacite = new Capacite();
    capacite.setNom(nom);
    capacite.setDescription("Description test");
    capacite.setTypeCapacite(typeCapacite);
    capacite.setValeurMin(1);
    capacite.setValeurMax(3);
    capacite.setDuree(1);
    capacite.setPrecision(100);
    capacite.setCoutEndurance(1);
    return capacite;
  }
}