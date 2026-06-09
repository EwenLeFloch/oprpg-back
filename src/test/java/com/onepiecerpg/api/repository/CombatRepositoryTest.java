package com.onepiecerpg.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.onepiecerpg.api.entity.Combat;
import com.onepiecerpg.api.entity.Ennemi;
import com.onepiecerpg.api.entity.Ile;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.StatutCombat;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.entity.Zone;

@DataJpaTest
class CombatRepositoryTest {

  @Autowired
  private CombatRepository combatRepository;

  @Autowired
  private ProgressionJoueurRepository progressionJoueurRepository;

  @Autowired
  private UtilisateurRepository utilisateurRepository;

  @Autowired
  private PersonnageRepository personnageRepository;

  @Autowired
  private IleRepository ileRepository;

  @Autowired
  private ZoneRepository zoneRepository;

  @Autowired
  private EnnemiRepository ennemiRepository;

  @Test
  void shouldFindByProgressionJoueurId() {
    ProgressionJoueur progression = progressionJoueurRepository.save(progression());
    Ennemi ennemi = ennemiRepository.save(ennemi(progression.getZone()));

    combatRepository.save(combat(progression, ennemi, StatutCombat.EN_COURS));
    combatRepository.save(combat(progression, ennemi, StatutCombat.VICTOIRE));

    List<Combat> result = combatRepository.findByProgressionJoueurId(progression.getId());

    assertThat(result).hasSize(2);
  }

  @Test
  void shouldFindByProgressionJoueurIdAndStatut() {
    ProgressionJoueur progression = progressionJoueurRepository.save(progression());
    Ennemi ennemi = ennemiRepository.save(ennemi(progression.getZone()));

    combatRepository.save(combat(progression, ennemi, StatutCombat.EN_COURS));
    combatRepository.save(combat(progression, ennemi, StatutCombat.VICTOIRE));

    Optional<Combat> result = combatRepository.findByProgressionJoueurIdAndStatut(
        progression.getId(),
        StatutCombat.EN_COURS);

    assertThat(result).isPresent();
    assertThat(result.get().getStatut()).isEqualTo(StatutCombat.EN_COURS);
  }

  private ProgressionJoueur progression() {
    Utilisateur utilisateur = utilisateurRepository.save(utilisateur());
    Personnage personnage = personnageRepository.save(personnage());
    Ile ile = ileRepository.save(ile());
    Zone zone = zoneRepository.save(zone(ile));

    ProgressionJoueur progression = new ProgressionJoueur();
    progression.setUtilisateur(utilisateur);
    progression.setPersonnage(personnage);
    progression.setZone(zone);
    progression.setVieActuelle(progression.getVieMax());
    progression.setEnduranceActuelle(progression.getEnduranceMax());

    return progression;
  }

  private Utilisateur utilisateur() {
    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setPseudo("luffy");
    utilisateur.setEmail("luffy@test.com");
    utilisateur.setMotDePasseHash("hashed-password");
    utilisateur.setRole("USER");
    return utilisateur;
  }

  private Personnage personnage() {
    Personnage personnage = new Personnage();
    personnage.setNom("Luffy");
    personnage.setDescription("Personnage de départ");
    personnage.setJouable(true);
    return personnage;
  }

  private Ile ile() {
    Ile ile = new Ile();
    ile.setNom("Dawn Island");
    ile.setImagePath("/images/dawn-island.png");
    ile.setDescription("Île de départ");
    ile.setNiveauRequis(1);
    return ile;
  }

  private Zone zone(Ile ile) {
    Zone zone = new Zone();
    zone.setNom("Village Fuschia");
    zone.setNiveauRequis(1);
    zone.setIle(ile);
    return zone;
  }

  private Ennemi ennemi(Zone zone) {
    Ennemi ennemi = new Ennemi();
    ennemi.setNom("Bandit");
    ennemi.setVieMax(20);
    ennemi.setPuissance(3);
    ennemi.setExperienceMin(5);
    ennemi.setExperienceMax(10);
    ennemi.setBoss(false);
    ennemi.setZone(zone);
    return ennemi;
  }

  private Combat combat(
      ProgressionJoueur progression,
      Ennemi ennemi,
      StatutCombat statut) {
    Combat combat = new Combat();
    combat.setProgressionJoueur(progression);
    combat.setEnnemi(ennemi);
    combat.setVieEnnemiActuelle(ennemi.getVieMax());
    combat.setStatut(statut);
    return combat;
  }
}