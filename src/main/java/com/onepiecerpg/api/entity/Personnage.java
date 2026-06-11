package com.onepiecerpg.api.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "personnage")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Personnage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String nom;

  private String description;

  @Builder.Default
  @Column(nullable = false)
  private boolean jouable = true;

  @Builder.Default
  @ManyToMany
  @JoinTable(name = "personnage_capacite", joinColumns = @JoinColumn(name = "personnage_id"), inverseJoinColumns = @JoinColumn(name = "capacite_id"))
  private Set<Capacite> capacites = new HashSet<>();
}
