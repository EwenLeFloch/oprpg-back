package com.onepiecerpg.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "progression_joueur")
@Getter
@Setter
public class ProgressionJoueur {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(1)
    @Column(nullable = false)
    private int niveau = 1;

    @PositiveOrZero
    @Column(nullable = false)
    private int experience = 0;

    @PositiveOrZero
    @Column(nullable = false)
    private int enduranceMax = 10;

    @PositiveOrZero
    @Column(nullable = false)
    private int enduranceActuelle;

    @Min(1)
    @Column(nullable = false)
    private int puissance = 1;

    @PositiveOrZero
    @Column(nullable = false)
    private int vieMax = 10;

    @PositiveOrZero
    @Column(nullable = false)
    private int vieActuelle;

    @PositiveOrZero
    @Column(nullable = false)
    private int berries = 0;

    @PositiveOrZero
    @Column(nullable = false)
    private Long prime = 0L;


    @OneToOne(optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false, unique = true)
    private Utilisateur utilisateur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "personnage_id", nullable = false)
    private Personnage personnage;

    @ManyToOne
    @JoinColumn(name = "faction_id")
    private Faction faction;
}
