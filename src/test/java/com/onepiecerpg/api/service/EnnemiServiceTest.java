package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.onepiecerpg.api.entity.Ennemi;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.EnnemiRepository;
import com.onepiecerpg.api.repository.ZoneRepository;

class EnnemiServiceTest {

    private EnnemiRepository ennemiRepository;
    private ZoneRepository zoneRepository;
    private EnnemiService ennemiService;

    @BeforeEach
    void setUp() {
        ennemiRepository = mock(EnnemiRepository.class);
        zoneRepository = mock(ZoneRepository.class);
        ennemiService = new EnnemiService(ennemiRepository, zoneRepository);
    }

    @Test
    void shouldGetEnemiesByZone() {
        when(zoneRepository.existsById(1L)).thenReturn(true);
        when(ennemiRepository.findByZoneId(1L)).thenReturn(List.of(new Ennemi()));

        assertThat(ennemiService.recupererEnnemisParZone(1L)).hasSize(1);
    }

    @Test
    void shouldGetClassicEnemiesByZone() {
        when(zoneRepository.existsById(1L)).thenReturn(true);
        when(ennemiRepository.findByZoneIdAndBossFalse(1L)).thenReturn(List.of(new Ennemi()));

        assertThat(ennemiService.recupererEnnemisClassiquesParZone(1L)).hasSize(1);
    }

    @Test
    void shouldRejectUnknownZone() {
        when(zoneRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> ennemiService.recupererEnnemisParZone(1L))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessage("Zone introuvable");
    }

    @Test
    void shouldGetEnemyById() {
        Ennemi ennemi = new Ennemi();
        ennemi.setId(1L);
        ennemi.setNom("Bandit");

        when(ennemiRepository.findById(1L)).thenReturn(Optional.of(ennemi));

        assertThat(ennemiService.recupererEnnemiParId(1L).getNom()).isEqualTo("Bandit");
    }
}