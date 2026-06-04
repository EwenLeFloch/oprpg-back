package com.onepiecerpg.api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "combat")
@Getter
@Setter
public class Combat {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "progression_joueur_id", nullable = false)
    private ProgressionJoueur progressionJoueur;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ennemi_id", nullable = false)
    private Ennemi ennemi;

    @PositiveOrZero
    @Column(nullable = false)
    private int vieEnnemiActuelle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutCombat statut = StatutCombat.EN_COURS;

    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();

    private LocalDateTime dateFin;
}
