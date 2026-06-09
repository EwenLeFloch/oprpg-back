package com.onepiecerpg.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.onepiecerpg.api.entity.Ile;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.ProgressionJoueur;
import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.entity.Zone;

@DataJpaTest
class ProgressionJoueurRepositoryTest {

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

  @Test
  void shouldFindByUtilisateur() {
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

    progressionJoueurRepository.save(progression);

    Optional<ProgressionJoueur> result = progressionJoueurRepository.findByUtilisateur(utilisateur);

    assertThat(result).isPresent();
    assertThat(result.get().getUtilisateur().getEmail()).isEqualTo("luffy@test.com");
    assertThat(result.get().getPersonnage().getNom()).isEqualTo("Luffy");
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
}