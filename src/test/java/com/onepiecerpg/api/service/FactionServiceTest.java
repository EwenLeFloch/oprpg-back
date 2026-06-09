package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.onepiecerpg.api.entity.Faction;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.FactionRepository;

class FactionServiceTest {

  private FactionRepository factionRepository;
  private FactionService factionService;

  @BeforeEach
  void setUp() {
    factionRepository = mock(FactionRepository.class);
    factionService = new FactionService(factionRepository);
  }

  @Test
  void shouldGetAllFactions() {
    when(factionRepository.findAll()).thenReturn(List.of(new Faction(), new Faction()));

    assertThat(factionService.recupererToutesLesFactions()).hasSize(2);
  }

  @Test
  void shouldGetFactionById() {
    Faction faction = new Faction();
    faction.setId(1L);
    faction.setNom("Pirate");

    when(factionRepository.findById(1L)).thenReturn(Optional.of(faction));

    assertThat(factionService.recupererFactionParId(1L).getNom()).isEqualTo("Pirate");
  }

  @Test
  void shouldThrowWhenFactionNotFoundById() {
    when(factionRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> factionService.recupererFactionParId(1L))
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("Faction non trouvée");
  }
}