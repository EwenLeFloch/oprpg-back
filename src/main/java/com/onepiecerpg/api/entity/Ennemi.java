package com.onepiecerpg.api.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ennemi")
@Getter
@Setter
public class Ennemi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Min(1)
    @Column(nullable = false)
    private int vieMax;

    @Min(1)
    @Column(nullable = false)
    private int puissance;

    @Min(1)
    @Column(nullable = false)
    private int experienceMin;

    @Min(1)
    @Column(nullable = false)
    private int experienceMax;

    @Column(nullable = false)
    private boolean boss = false;

    @ManyToOne(optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;

    @ManyToMany
    @JoinTable(
        name = "ennemi_move",
        joinColumns = @JoinColumn(name = "ennemi_id"),
        inverseJoinColumns = @JoinColumn(name = "move_id")
    )
    private Set<Move> moves = new HashSet<>();
}
