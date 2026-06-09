package com.onepiecerpg.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Entity
@Table(name = "progression_joueur")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressionJoueur {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Builder.Default
  @Min(1)
  @Column(nullable = false)
  private int niveau = 1;

  @Builder.Default
  @PositiveOrZero
  @Column(nullable = false)
  private int experience = 0;

  @Builder.Default
  @PositiveOrZero
  @Column(nullable = false)
  private int enduranceMax = 10;

  @PositiveOrZero
  @Column(nullable = false)
  private int enduranceActuelle;

  @Builder.Default
  @Min(1)
  @Column(nullable = false)
  private int puissance = 1;

  @Builder.Default
  @PositiveOrZero
  @Column(nullable = false)
  private int vieMax = 10;

  @PositiveOrZero
  @Column(nullable = false)
  private int vieActuelle;

  @Builder.Default
  @PositiveOrZero
  @Column(nullable = false)
  private int berries = 0;

  @Builder.Default
  @PositiveOrZero
  @Column(nullable = false)
  private Long prime = 0L;

  @OneToOne(optional = false)
  @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
  private Utilisateur utilisateur;

  @ManyToOne(optional = false)
  @JoinColumn(name = "personnage_id", nullable = false)
  private Personnage personnage;

  @ManyToOne(optional = false)
  @JoinColumn(name = "zone_id", nullable = false)
  private Zone zone;

  @ManyToOne
  @JoinColumn(name = "faction_id")
  private Faction faction;
}
