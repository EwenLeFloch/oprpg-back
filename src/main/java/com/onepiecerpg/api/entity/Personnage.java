package com.onepiecerpg.api.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "personnage")
@Getter
@Setter
public class Personnage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    private String description;

    @Column(nullable = false)
    private boolean jouable = true;

    @ManyToMany
    @JoinTable(
        name = "personnage_move",
        joinColumns = @JoinColumn(name = "personnage_id"),
        inverseJoinColumns = @JoinColumn(name = "move_id")
    )
    private Set<Move> moves = new HashSet<>();
}
