package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.onepiecerpg.api.dto.ProgressionJoueurResponse;
import com.onepiecerpg.api.entity.Faction;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.repository.FactionRepository;
import com.onepiecerpg.api.repository.PersonnageRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;

class ProgressionJoueurServiceTest {

    private ProgressionJoueurRepository progressionJoueurRepository;
    private PersonnageRepository personnageRepository;
    private UtilisateurRepository utilisateurRepository;
    private FactionRepository factionRepository;
    private ProgressionJoueurService progressionJoueurService;

    @BeforeEach
    void setUp() {
        progressionJoueurRepository = mock(ProgressionJoueurRepository.class);
        personnageRepository = mock(PersonnageRepository.class);
        utilisateurRepository = mock(UtilisateurRepository.class);
        factionRepository = mock(FactionRepository.class);

        progressionJoueurService = new ProgressionJoueurService(
                progressionJoueurRepository,
                personnageRepository,
                utilisateurRepository,
                factionRepository
        );

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("test@test.com", null)
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateInitialProgression() {
        Utilisateur utilisateur = utilisateur();
        Personnage personnage = personnage();

        when(personnageRepository.findByNom("Luffy")).thenReturn(Optional.of(personnage));
        when(progressionJoueurRepository.save(any(ProgressionJoueur.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProgressionJoueur progression = progressionJoueurService.creerProgressionInitiale(utilisateur);

        assertThat(progression.getUtilisateur()).isEqualTo(utilisateur);
        assertThat(progression.getPersonnage()).isEqualTo(personnage);
        assertThat(progression.getVieActuelle()).isEqualTo(progression.getVieMax());
        assertThat(progression.getEnduranceActuelle()).isEqualTo(progression.getEnduranceMax());
    }

    @Test
    void shouldGetConnectedProgression() {
        ProgressionJoueur progression = progression();

        mockProgressionConnectee(progression);

        ProgressionJoueurResponse response = progressionJoueurService.getProgressionConnectee();

        assertThat(response.niveau()).isEqualTo(1);
        assertThat(response.personnage()).isEqualTo("Luffy");
        assertThat(response.faction()).isNull();
    }

    @Test
    void shouldChooseFaction() {
        ProgressionJoueur progression = progression();

        Faction faction = new Faction();
        faction.setId(1L);
        faction.setNom("Pirate");

        mockProgressionConnectee(progression);
        when(factionRepository.findById(1L)).thenReturn(Optional.of(faction));
        when(progressionJoueurRepository.save(progression)).thenReturn(progression);

        ProgressionJoueurResponse response = progressionJoueurService.choisirFaction(1L);

        assertThat(response.faction()).isEqualTo("Pirate");
        assertThat(progression.getFaction()).isEqualTo(faction);
    }

    @Test
    void shouldRejectFactionChoiceWhenAlreadyChosen() {
        ProgressionJoueur progression = progression();

        Faction faction = new Faction();
        faction.setId(1L);
        progression.setFaction(faction);

        mockProgressionConnectee(progression);

        assertThatThrownBy(() -> progressionJoueurService.choisirFaction(2L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("La faction a déjà été choisie");

        verify(factionRepository, never()).findById(anyLong());
    }

    private void mockProgressionConnectee(ProgressionJoueur progression) {
        Utilisateur utilisateur = utilisateur();

        when(utilisateurRepository.findByEmail("test@test.com")).thenReturn(Optional.of(utilisateur));
        when(progressionJoueurRepository.findByUtilisateur(utilisateur)).thenReturn(Optional.of(progression));
    }

    private Utilisateur utilisateur() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(1L);
        utilisateur.setEmail("test@test.com");
        utilisateur.setPseudo("luffy");
        utilisateur.setRole("USER");
        return utilisateur;
    }

    private Personnage personnage() {
        Personnage personnage = new Personnage();
        personnage.setId(1L);
        personnage.setNom("Luffy");
        return personnage;
    }

    private ProgressionJoueur progression() {
        ProgressionJoueur progression = new ProgressionJoueur();
        progression.setId(1L);
        progression.setUtilisateur(utilisateur());
        progression.setPersonnage(personnage());
        progression.setNiveau(1);
        progression.setExperience(0);
        progression.setEnduranceMax(10);
        progression.setEnduranceActuelle(10);
        progression.setPuissance(1);
        progression.setVieMax(10);
        progression.setVieActuelle(10);
        progression.setBerries(0);
        progression.setPrime(0L);
        return progression;
    }
}