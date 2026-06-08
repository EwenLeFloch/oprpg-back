package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.onepiecerpg.api.entity.Ile;
import com.onepiecerpg.api.entity.Zone;
import com.onepiecerpg.api.repository.IleRepository;
import com.onepiecerpg.api.repository.ZoneRepository;

class MondeServiceTest {

    private IleRepository ileRepository;
    private ZoneRepository zoneRepository;
    private MondeService mondeService;

    @BeforeEach
    void setUp() {
        ileRepository = mock(IleRepository.class);
        zoneRepository = mock(ZoneRepository.class);
        mondeService = new MondeService(ileRepository, zoneRepository);
    }

    @Test
    void shouldGetAllIles() {
        when(ileRepository.findAll()).thenReturn(List.of(new Ile()));

        assertThat(mondeService.recupererToutesLesIles()).hasSize(1);
    }

    @Test
    void shouldGetIleById() {
        Ile ile = new Ile();
        ile.setId(1L);
        ile.setNom("Dawn Island");

        when(ileRepository.findById(1L)).thenReturn(Optional.of(ile));

        assertThat(mondeService.recupererIleParId(1L).getNom()).isEqualTo("Dawn Island");
    }

    @Test
    void shouldGetZonesByIle() {
        when(ileRepository.existsById(1L)).thenReturn(true);
        when(zoneRepository.findByIleId(1L)).thenReturn(List.of(new Zone()));

        assertThat(mondeService.recupererZonesParIle(1L)).hasSize(1);
    }

    @Test
    void shouldRejectUnknownIleForZones() {
        when(ileRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> mondeService.recupererZonesParIle(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Île non trouvée");
    }

    @Test
    void shouldCheckZoneAccess() {
        Zone zone = new Zone();
        zone.setNiveauRequis(3);

        assertThat(mondeService.joueurPeutAccederZone(3, zone)).isTrue();
        assertThat(mondeService.joueurPeutAccederZone(2, zone)).isFalse();
    }
}