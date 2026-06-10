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
import com.onepiecerpg.api.entity.Move;
import com.onepiecerpg.api.entity.News;
import com.onepiecerpg.api.entity.Personnage;
import com.onepiecerpg.api.entity.TypeMove;
import com.onepiecerpg.api.entity.Zone;
import com.onepiecerpg.api.repository.EnnemiRepository;
import com.onepiecerpg.api.repository.FactionRepository;
import com.onepiecerpg.api.repository.IleRepository;
import com.onepiecerpg.api.repository.MoveRepository;
import com.onepiecerpg.api.repository.NewsRepository;
import com.onepiecerpg.api.repository.PersonnageRepository;
import com.onepiecerpg.api.repository.ZoneRepository;

@Component
public class DataInitializer implements CommandLineRunner {

  private final IleRepository ileRepository;
  private final ZoneRepository zoneRepository;
  private final FactionRepository factionRepository;
  private final MoveRepository moveRepository;
  private final PersonnageRepository personnageRepository;
  private final EnnemiRepository ennemiRepository;
  private final NewsRepository newsRepository;
  private final Clock clock;

  public DataInitializer(
      IleRepository ileRepository,
      ZoneRepository zoneRepository,
      FactionRepository factionRepository,
      MoveRepository moveRepository,
      PersonnageRepository personnageRepository,
      EnnemiRepository ennemiRepository,
      NewsRepository newsRepository,
      Clock clock) {
    this.ileRepository = ileRepository;
    this.zoneRepository = zoneRepository;
    this.factionRepository = factionRepository;
    this.moveRepository = moveRepository;
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

    Move coupDePoing = creerMoveSiAbsent(
        Move.builder()
            .nom("Coup de poing")
            .description("Une attaque simple au corps-à-corps.")
            .typeMove(TypeMove.ATTAQUE)
            .valeurMin(4)
            .valeurMax(7)
            .duree(1)
            .precision(95)
            .coutEndurance(1)
            .build());

    Move coupDePied = creerMoveSiAbsent(
        Move.builder()
            .nom("Coup de pied")
            .description("Une attaque plus puissante mais légèrement moins précise.")
            .typeMove(TypeMove.ATTAQUE)
            .valeurMin(6)
            .valeurMax(10)
            .duree(1)
            .precision(85)
            .coutEndurance(2)
            .build());

    Move lait = creerMoveSiAbsent(
        Move.builder()
            .nom("Bouteille de lait")
            .description("Restaure quelques points de vie.")
            .typeMove(TypeMove.SOIN)
            .valeurMin(4)
            .valeurMax(8)
            .duree(1)
            .precision(100)
            .coutEndurance(2)
            .build());

    Move intimidation = creerMoveSiAbsent(
        Move.builder()
            .nom("Intimidation")
            .description("Une technique de pression destinée à déstabiliser l'adversaire.")
            .typeMove(TypeMove.BOOST)
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
            .experienceMin(4)
            .experienceMax(6)
            .boss(false)
            .zone(villageFuschia)
            .moves(Set.of(coupDePoing))
            .build());

    creerEnnemiSiAbsent(
        Ennemi.builder()
            .nom("Bandit")
            .vieMax(20)
            .puissance(3)
            .experienceMin(6)
            .experienceMax(10)
            .boss(false)
            .zone(villageFuschia)
            .moves(Set.of(coupDePoing, coupDePied))
            .build());

    creerEnnemiSiAbsent(
        Ennemi.builder()
            .nom("Bandit robuste")
            .vieMax(28)
            .puissance(4)
            .experienceMin(10)
            .experienceMax(14)
            .boss(false)
            .zone(villageFuschia)
            .moves(Set.of(coupDePoing, coupDePied))
            .build());

    creerEnnemiSiAbsent(
        Ennemi.builder()
            .nom("Higuma")
            .vieMax(45)
            .puissance(6)
            .experienceMin(30)
            .experienceMax(40)
            .boss(true)
            .zone(villageFuschia)
            .moves(Set.of(coupDePoing, coupDePied, intimidation))
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
    ile.setImagePath("/images/iles/dawn-island.png");
    ile.setDescription("Île de départ située dans East Blue.");
    ile.setNiveauRequis(1);

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

  private Move creerMoveSiAbsent(Move move) {
    Move moveExistant = moveRepository.findByNom(move.getNom()).orElse(null);

    if (moveExistant != null) {
      return moveExistant;
    }

    return moveRepository.save(move);
  }

  private void creerPersonnageLuffySiAbsent(Set<Move> moves) {
    Personnage personnageExistant = personnageRepository.findByNom("Luffy").orElse(null);

    if (personnageExistant != null) {
      personnageExistant.getMoves().addAll(moves);
      personnageRepository.save(personnageExistant);
      return;
    }

    Personnage personnage = new Personnage();
    personnage.setNom("Luffy");
    personnage.setDescription("Personnage de départ du joueur.");
    personnage.setJouable(true);
    personnage.setMoves(moves);

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