package com.onepiecerpg.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "move")
@Getter
@Setter
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    String nom;

    @Column(length = 254)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeMove typeMove;

    @PositiveOrZero
    @Column(nullable = false)
    private int valeurMove;

    @Min(1)
    @Column(nullable = false)
    private int duree = 1;

    @Min(0)
    @Max(100)
    private Integer precision = 100;

    @PositiveOrZero
    @Column(nullable = false)
    private int coutEndurance;
}
