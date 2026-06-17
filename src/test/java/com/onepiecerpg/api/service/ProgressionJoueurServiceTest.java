package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.onepiecerpg.api.dto.ProgressionJoueurResponse;
import com.onepiecerpg.api.entity.Faction;
import com.onepiecerpg.api.entity.Ile;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.entity.Zone;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.FactionRepository;
import com.onepiecerpg.api.repository.PersonnageRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;
import com.onepiecerpg.api.repository.ZoneRepository;

class ProgressionJoueurServiceTest {

  private ProgressionJoueurRepository progressionJoueurRepository;
  private PersonnageRepository personnageRepository;
  private UtilisateurRepository utilisateurRepository;
  private FactionRepository factionRepository;
  private ZoneRepository zoneRepository;
  private ProgressionJoueurService progressionJoueurService;

  @BeforeEach
  void setUp() {
    progressionJoueurRepository = mock(ProgressionJoueurRepository.class);
    personnageRepository = mock(PersonnageRepository.class);
    utilisateurRepository = mock(UtilisateurRepository.class);
    factionRepository = mock(FactionRepository.class);
    zoneRepository = mock(ZoneRepository.class);

    progressionJoueurService = new ProgressionJoueurService(
        progressionJoueurRepository,
        personnageRepository,
        utilisateurRepository,
        factionRepository,
        zoneRepository);

    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("test@test.com", null));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void shouldCreateInitialProgression() {
    Utilisateur utilisateur = utilisateur();
    Personnage personnage = personnage();
    Zone zone = zone();

    when(personnageRepository.findByNom("Luffy")).thenReturn(Optional.of(personnage));
    when(zoneRepository.findByNom("Village Fuschia")).thenReturn(Optional.of(zone));
    when(progressionJoueurRepository.save(any(ProgressionJoueur.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    ProgressionJoueur progression = progressionJoueurService.creerProgressionInitiale(utilisateur);

    assertThat(progression.getUtilisateur()).isEqualTo(utilisateur);
    assertThat(progression.getPersonnage()).isEqualTo(personnage);
    assertThat(progression.getZone()).isEqualTo(zone);
    assertThat(progression.getVieActuelle()).isEqualTo(progression.getVieMax());
    assertThat(progression.getEnduranceActuelle()).isEqualTo(progression.getEnduranceMax());
  }

  @Test
  void shouldRejectInitialProgressionWhenDefaultCharacterNotFound() {
    Utilisateur utilisateur = utilisateur();

    when(personnageRepository.findByNom("Luffy")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> progressionJoueurService.creerProgressionInitiale(utilisateur))
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("Personnage de départ non trouvé");

    verify(zoneRepository, never()).findByNom(anyString());
    verify(progressionJoueurRepository, never()).save(any());
  }

  @Test
  void shouldRejectInitialProgressionWhenDefaultZoneNotFound() {
    Utilisateur utilisateur = utilisateur();
    Personnage personnage = personnage();

    when(personnageRepository.findByNom("Luffy")).thenReturn(Optional.of(personnage));
    when(zoneRepository.findByNom("Village Fuschia")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> progressionJoueurService.creerProgressionInitiale(utilisateur))
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("Zone de départ non trouvée");

    verify(progressionJoueurRepository, never()).save(any());
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

  @Test
  void shouldRejectFactionChoiceWhenFactionNotFound() {
    ProgressionJoueur progression = progression();

    mockProgressionConnectee(progression);
    when(factionRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> progressionJoueurService.choisirFaction(1L))
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("Faction non trouvée");

    verify(progressionJoueurRepository, never()).save(any());
  }

  @Test
  void shouldRestoreEnduranceOnRest() {
    ProgressionJoueur progression = progression();
    progression.setEnduranceActuelle(2); // endurance entamée

    mockProgressionConnectee(progression);
    when(progressionJoueurRepository.save(progression)).thenReturn(progression);

    ProgressionJoueurResponse response = progressionJoueurService.seReposer();

    assertThat(response.enduranceActuelle()).isEqualTo(progression.getEnduranceMax());
    assertThat(progression.getEnduranceActuelle()).isEqualTo(progression.getEnduranceMax());
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

  private Ile ile() {
    Ile ile = new Ile();
    ile.setId(1L);
    ile.setNom("Dawn Island");
    ile.setNomImage("dawn-island");
    ile.setDescription("Île de départ");
    ile.setNiveauRequis(1);
    ile.setPositionX(1500);
    ile.setPositionY(100);
    return ile;
  }

  private Zone zone() {
    Zone zone = new Zone();
    zone.setId(1L);
    zone.setNom("Village Fuschia");
    zone.setNiveauRequis(1);
    zone.setIle(ile());
    return zone;
  }

  private ProgressionJoueur progression() {
    ProgressionJoueur progression = new ProgressionJoueur();
    progression.setId(1L);
    progression.setUtilisateur(utilisateur());
    progression.setPersonnage(personnage());
    progression.setZone(zone());
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