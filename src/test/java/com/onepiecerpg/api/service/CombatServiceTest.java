package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.onepiecerpg.api.dto.CombatResponse;
import com.onepiecerpg.api.entity.Combat;
import com.onepiecerpg.api.entity.Ennemi;
import com.onepiecerpg.api.entity.Move;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.StatutCombat;
import com.onepiecerpg.api.entity.TypeMove;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.repository.CombatRepository;
import com.onepiecerpg.api.repository.EnnemiRepository;
import com.onepiecerpg.api.repository.ProgressionJoueurRepository;
import com.onepiecerpg.api.repository.UtilisateurRepository;

class CombatServiceTest {

    private CombatRepository combatRepository;
    private EnnemiRepository ennemiRepository;
    private ProgressionJoueurRepository progressionJoueurRepository;
    private UtilisateurRepository utilisateurRepository;
    private CombatService combatService;

    @BeforeEach
    void setUp() {
        combatRepository = mock(CombatRepository.class);
        ennemiRepository = mock(EnnemiRepository.class);
        progressionJoueurRepository = mock(ProgressionJoueurRepository.class);
        utilisateurRepository = mock(UtilisateurRepository.class);

        combatService = new CombatService(
                combatRepository,
                ennemiRepository,
                progressionJoueurRepository,
                utilisateurRepository
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
    @DisplayName("Doit démarrer un combat")
    void shouldStartCombat() {
        ProgressionJoueur progression = progression();
        Ennemi ennemi = ennemi(1L, "Bandit", 20, 3);

        mockProgressionConnectee(progression);
        when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
                .thenReturn(Optional.empty());
        when(ennemiRepository.findById(1L)).thenReturn(Optional.of(ennemi));
        when(combatRepository.save(any(Combat.class))).thenAnswer(invocation -> {
            Combat combat = invocation.getArgument(0);
            combat.setId(1L);
            return combat;
        });

        CombatResponse response = combatService.demarrerCombat(1L);

        assertThat(response.combatId()).isEqualTo(1L);
        assertThat(response.ennemi()).isEqualTo("Bandit");
        assertThat(response.vieEnnemiActuelle()).isEqualTo(20);
        assertThat(response.vieJoueurActuelle()).isEqualTo(30);
        assertThat(response.statut()).isEqualTo(StatutCombat.EN_COURS);

        verify(combatRepository).save(any(Combat.class));
    }

    @Test
    @DisplayName("Doit refuser de démarrer un combat si un combat est déjà en cours")
    void shouldRejectStartCombatWhenCombatAlreadyExists() {
        ProgressionJoueur progression = progression();

        mockProgressionConnectee(progression);
        when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
                .thenReturn(Optional.of(new Combat()));

        assertThatThrownBy(() -> combatService.demarrerCombat(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Un combat est déjà en cours");

        verify(ennemiRepository, never()).findById(anyLong());
        verify(combatRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit attaquer un ennemi")
    void shouldAttackEnemy() {
        ProgressionJoueur progression = progression();
        Move attaque = move(1L, "Coup de poing", TypeMove.ATTAQUE, 5);
        progression.getPersonnage().setMoves(new HashSet<>(Set.of(attaque)));

        Ennemi ennemi = ennemi(1L, "Bandit", 20, 3);
        Combat combat = combat(progression, ennemi, 20);

        mockProgressionConnectee(progression);
        when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
                .thenReturn(Optional.of(combat));

        CombatResponse response = combatService.utiliserMove(1L);

        assertThat(response.vieEnnemiActuelle()).isEqualTo(10);
        assertThat(response.vieJoueurActuelle()).isEqualTo(27);
        assertThat(response.statut()).isEqualTo(StatutCombat.EN_COURS);

        verify(combatRepository).save(combat);
        verify(progressionJoueurRepository).save(progression);
    }

    @Test
    @DisplayName("Doit terminer le combat en victoire")
    void shouldWinCombat() {
        ProgressionJoueur progression = progression();
        Move attaque = move(1L, "Gros coup", TypeMove.ATTAQUE, 50);
        progression.getPersonnage().setMoves(new HashSet<>(Set.of(attaque)));

        Ennemi ennemi = ennemi(1L, "Bandit", 20, 3);
        Combat combat = combat(progression, ennemi, 20);

        mockProgressionConnectee(progression);
        when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
                .thenReturn(Optional.of(combat));

        CombatResponse response = combatService.utiliserMove(1L);

        assertThat(response.vieEnnemiActuelle()).isZero();
        assertThat(response.statut()).isEqualTo(StatutCombat.VICTOIRE);
        assertThat(progression.getExperience()).isEqualTo(10);
        assertThat(progression.getBerries()).isEqualTo(100);
        assertThat(progression.getVieActuelle()).isEqualTo(30);
    }

    @Test
    @DisplayName("Doit soigner le joueur sans dépasser sa vie maximale")
    void shouldHealPlayerWithoutExceedingMaxHp() {
        ProgressionJoueur progression = progression();
        progression.setVieActuelle(20);

        Move soin = move(1L, "Lait", TypeMove.SOIN, 50);
        progression.getPersonnage().setMoves(new HashSet<>(Set.of(soin)));

        Ennemi ennemi = ennemi(1L, "Bandit", 20, 3);
        Combat combat = combat(progression, ennemi, 20);

        mockProgressionConnectee(progression);
        when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
                .thenReturn(Optional.of(combat));

        CombatResponse response = combatService.utiliserMove(1L);

        assertThat(response.vieJoueurActuelle()).isEqualTo(27);
        assertThat(progression.getVieActuelle()).isEqualTo(27);
        assertThat(response.statut()).isEqualTo(StatutCombat.EN_COURS);
    }

    @Test
    @DisplayName("Doit terminer le combat en défaite")
    void shouldLoseCombat() {
        ProgressionJoueur progression = progression();
        progression.setVieActuelle(2);

        Move attaque = move(1L, "Petit coup", TypeMove.ATTAQUE, 1);
        progression.getPersonnage().setMoves(new HashSet<>(Set.of(attaque)));

        Ennemi ennemi = ennemi(1L, "Bandit", 50, 10);
        Combat combat = combat(progression, ennemi, 50);

        mockProgressionConnectee(progression);
        when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
                .thenReturn(Optional.of(combat));

        CombatResponse response = combatService.utiliserMove(1L);

        assertThat(response.statut()).isEqualTo(StatutCombat.DEFAITE);
        assertThat(response.vieJoueurActuelle()).isZero();
    }

    @Test
    @DisplayName("Doit fuir un combat")
    void shouldFleeCombat() {
        ProgressionJoueur progression = progression();
        Ennemi ennemi = ennemi(1L, "Bandit", 20, 3);
        Combat combat = combat(progression, ennemi, 20);

        mockProgressionConnectee(progression);
        when(combatRepository.findByProgressionJoueurIdAndStatut(1L, StatutCombat.EN_COURS))
                .thenReturn(Optional.of(combat));

        CombatResponse response = combatService.fuirCombat();

        assertThat(response.statut()).isEqualTo(StatutCombat.FUITE);
        verify(combatRepository).save(combat);
    }

    private void mockProgressionConnectee(ProgressionJoueur progression) {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@test.com");

        when(utilisateurRepository.findByEmail("test@test.com"))
                .thenReturn(Optional.of(utilisateur));

        when(progressionJoueurRepository.findByUtilisateur(utilisateur))
                .thenReturn(Optional.of(progression));
    }

    private ProgressionJoueur progression() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setEmail("test@test.com");

        Personnage personnage = new Personnage();
        personnage.setNom("Luffy");

        ProgressionJoueur progression = new ProgressionJoueur();
        progression.setId(1L);
        progression.setUtilisateur(utilisateur);
        progression.setPersonnage(personnage);
        progression.setNiveau(1);
        progression.setExperience(0);
        progression.setPuissance(5);
        progression.setVieMax(30);
        progression.setVieActuelle(30);
        progression.setEnduranceMax(10);
        progression.setEnduranceActuelle(10);
        progression.setBerries(0);
        progression.setPrime(0L);

        return progression;
    }

    private Ennemi ennemi(Long id, String nom, int vieMax, int puissance) {
        Ennemi ennemi = new Ennemi();
        ennemi.setId(id);
        ennemi.setNom(nom);
        ennemi.setVieMax(vieMax);
        ennemi.setPuissance(puissance);
        return ennemi;
    }

    private Move move(Long id, String nom, TypeMove typeMove, int valeurMove) {
        Move move = new Move();
        move.setId(id);
        move.setNom(nom);
        move.setTypeMove(typeMove);
        move.setValeurMove(valeurMove);
        move.setCoutEndurance(1);
        return move;
    }

    private Combat combat(ProgressionJoueur progression, Ennemi ennemi, int vieEnnemiActuelle) {
        Combat combat = new Combat();
        combat.setId(1L);
        combat.setProgressionJoueur(progression);
        combat.setEnnemi(ennemi);
        combat.setVieEnnemiActuelle(vieEnnemiActuelle);
        combat.setStatut(StatutCombat.EN_COURS);
        return combat;
    }
}