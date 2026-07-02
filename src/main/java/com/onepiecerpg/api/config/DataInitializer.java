package com.onepiecerpg.api.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.onepiecerpg.api.entity.Ennemi;
import com.onepiecerpg.api.entity.Faction;
import com.onepiecerpg.api.entity.Ile;
import com.onepiecerpg.api.entity.Capacite;
import com.onepiecerpg.api.entity.News;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.TypeCapacite;
import com.onepiecerpg.api.entity.Zone;
import com.onepiecerpg.api.repository.EnnemiRepository;
import com.onepiecerpg.api.repository.FactionRepository;
import com.onepiecerpg.api.repository.IleRepository;
import com.onepiecerpg.api.repository.CapaciteRepository;
import com.onepiecerpg.api.repository.NewsRepository;
import com.onepiecerpg.api.repository.PersonnageRepository;
import com.onepiecerpg.api.repository.ZoneRepository;

@Component
public class DataInitializer implements CommandLineRunner {

  private final IleRepository ileRepository;
  private final ZoneRepository zoneRepository;
  private final FactionRepository factionRepository;
  private final CapaciteRepository capaciteRepository;
  private final PersonnageRepository personnageRepository;
  private final EnnemiRepository ennemiRepository;
  private final NewsRepository newsRepository;
  private final Clock clock;

  public DataInitializer(
      IleRepository ileRepository,
      ZoneRepository zoneRepository,
      FactionRepository factionRepository,
      CapaciteRepository capaciteRepository,
      PersonnageRepository personnageRepository,
      EnnemiRepository ennemiRepository,
      NewsRepository newsRepository,
      Clock clock) {
    this.ileRepository = ileRepository;
    this.zoneRepository = zoneRepository;
    this.factionRepository = factionRepository;
    this.capaciteRepository = capaciteRepository;
    this.personnageRepository = personnageRepository;
    this.ennemiRepository = ennemiRepository;
    this.newsRepository = newsRepository;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void run(String... args) {
    Ile dawnIsland = creerIleSiAbsente();
    Zone villageFuschia = creerZoneSiAbsente(dawnIsland);

    creerFactionsSiAbsentes();

    Capacite coupDePoing = creerCapaciteSiAbsent(
        Capacite.builder()
            .nom("Coup de poing")
            .description("Une attaque simple au corps-à-corps.")
            .typeCapacite(TypeCapacite.ATTAQUE)
            .valeurMin(4)
            .valeurMax(7)
            .duree(1)
            .precision(95)
            .coutEndurance(1)
            .build());

    Capacite coupDePied = creerCapaciteSiAbsent(
        Capacite.builder()
            .nom("Coup de pied")
            .description("Une attaque plus puissante mais légèrement moins précise.")
            .typeCapacite(TypeCapacite.ATTAQUE)
            .valeurMin(6)
            .valeurMax(10)
            .duree(1)
            .precision(85)
            .coutEndurance(2)
            .build());

    Capacite lait = creerCapaciteSiAbsent(
        Capacite.builder()
            .nom("Bouteille de lait")
            .description("Restaure quelques points de vie.")
            .typeCapacite(TypeCapacite.SOIN)
            .valeurMin(4)
            .valeurMax(8)
            .duree(1)
            .precision(100)
            .coutEndurance(2)
            .build());

    Capacite intimidation = creerCapaciteSiAbsent(
        Capacite.builder()
            .nom("Intimidation")
            .description("Une technique de pression destinée à déstabiliser l'adversaire.")
            .typeCapacite(TypeCapacite.BOOST)
            .valeurMin(1)
            .valeurMax(2)
            .duree(1)
            .precision(100)
            .coutEndurance(1)
            .build());

    creerPersonnageLuffySiAbsent(Set.of(coupDePoing, coupDePied, lait));

   creerEnnemiSiAbsent(
        Ennemi.builder()
            .nom("Bandit faible")
            .vieMax(12)
            .puissance(2)
            .boss(false)
            .niveauRequis(1)
            .nomImage("bandit-faible")
            .zone(villageFuschia)
            .capacites(Set.of(coupDePoing))
            .build());

    creerEnnemiSiAbsent(
        Ennemi.builder()
            .nom("Bandit")
            .vieMax(20)
            .puissance(3)
            .boss(false)
            .niveauRequis(1)
            .nomImage("bandit")
            .zone(villageFuschia)
            .capacites(Set.of(coupDePoing, coupDePied))
            .build());

    creerEnnemiSiAbsent(
        Ennemi.builder()
            .nom("Bandit robuste")
            .vieMax(28)
            .puissance(4)
            .boss(false)
            .niveauRequis(1)
            .nomImage("bandit-robuste")
            .zone(villageFuschia)
            .capacites(Set.of(coupDePoing, coupDePied))
            .build());

    creerEnnemiSiAbsent(
        Ennemi.builder()
            .nom("Higuma")
            .vieMax(45)
            .puissance(6)
            .boss(true)
            .niveauRequis(5)
            .nomImage("higuma")
            .zone(villageFuschia)
            .capacites(Set.of(coupDePoing, coupDePied, intimidation))
            .build());
            
    creerNewsSiAbsente();
  }

  private Ile creerIleSiAbsente() {
    Ile ileExistante = ileRepository.findByNom("Dawn Island").orElse(null);

    if (ileExistante != null) {
      return ileExistante;
    }

    Ile ile = new Ile();
    ile.setNom("Dawn Island");
    ile.setNomImage("dawn-island");
    ile.setDescription("Île de départ située dans East Blue.");
    ile.setNiveauRequis(1);
    ile.setPositionX(1450);
    ile.setPositionY(800);

    return ileRepository.save(ile);
  }

  private Zone creerZoneSiAbsente(Ile ile) {
    Zone zoneExistante = zoneRepository.findByNom("Village Fuschia").orElse(null);

    if (zoneExistante != null) {
      return zoneExistante;
    }

    Zone zone = new Zone();
    zone.setNom("Village Fuschia");
    zone.setNiveauRequis(1);
    zone.setIle(ile);

    return zoneRepository.save(zone);
  }

  private void creerFactionsSiAbsentes() {
    creerFactionSiAbsente(
        "Pirate",
        "Les pirates parcourent les mers à la recherche de liberté, de puissance et de trésors.");

    creerFactionSiAbsente(
        "Marine",
        "La Marine maintient l'ordre sur les mers et combat les criminels.");

    creerFactionSiAbsente(
        "Révolutionnaire",
        "Les révolutionnaires luttent contre l'ordre établi et le Gouvernement Mondial.");

    creerFactionSiAbsente(
        "Chasseur de primes",
        "Les chasseurs de primes traquent les criminels pour gagner des récompenses.");
  }

  private void creerFactionSiAbsente(String nom, String description) {
    Faction factionExistante = factionRepository.findByNom(nom).orElse(null);

    if (factionExistante == null) {
      Faction faction = new Faction();
      faction.setNom(nom);
      faction.setDescription(description);

      factionRepository.save(faction);
    }
  }

  private Capacite creerCapaciteSiAbsent(Capacite capacite) {
    Capacite capaciteExistant = capaciteRepository.findByNom(capacite.getNom()).orElse(null);

    if (capaciteExistant != null) {
      return capaciteExistant;
    }

    return capaciteRepository.save(capacite);
  }

  private void creerPersonnageLuffySiAbsent(Set<Capacite> capacites) {
    Personnage personnageExistant = personnageRepository.findByNom("Luffy").orElse(null);

    if (personnageExistant != null) {
      personnageExistant.getCapacites().addAll(capacites);
      personnageRepository.save(personnageExistant);
      return;
    }

    Personnage personnage = new Personnage();
    personnage.setNom("Luffy");
    personnage.setNomImage("luffy");
    personnage.setDescription("Personnage de départ du joueur.");
    personnage.setJouable(true);
    personnage.setCapacites(capacites);

    personnageRepository.save(personnage);
  }

  private void creerEnnemiSiAbsent(Ennemi ennemi) {
    Ennemi ennemiExistant = ennemiRepository.findByNom(ennemi.getNom()).orElse(null);

    if (ennemiExistant == null) {
      ennemiRepository.save(ennemi);
    }
  }

  private void creerNewsSiAbsente() {
    if (newsRepository.count() == 0) {
      News news = new News();
      news.setTitre("Bienvenue sur One Piece RPG");
      news.setContenu(
          "Le MVP est disponible : crée ton compte, choisis ta faction et combats les bandits de Dawn Island.");
      news.setDateCreation(LocalDateTime.now(clock));

      newsRepository.save(news);
    }
  }
}