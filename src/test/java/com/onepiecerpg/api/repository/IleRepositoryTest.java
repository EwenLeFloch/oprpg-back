package com.onepiecerpg.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.onepiecerpg.api.entity.Ile;

@DataJpaTest
class IleRepositoryTest {

  @Autowired
  private IleRepository ileRepository;

  @Test
  void shouldFindByNom() {
    Ile ile = new Ile();
    ile.setNom("Dawn Island");
    ile.setNom("dawn-island");
    ile.setDescription("Île de départ");
    ile.setNiveauRequis(1);
    ile.setPositionX(1500);
    ile.setPositionY(100);

    ileRepository.save(ile);

    Optional<Ile> result = ileRepository.findByNom("Dawn Island");

    assertThat(result).isPresent();
    assertThat(result.get().getNiveauRequis()).isEqualTo(1);
  }
}