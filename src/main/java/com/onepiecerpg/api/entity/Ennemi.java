package com.onepiecerpg.api.entity;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "ennemi")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
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

  @Builder.Default
  @Column(nullable = false)
  private boolean boss = false;

  @ManyToOne(optional = false)
  @JoinColumn(name = "zone_id", nullable = false)
  private Zone zone;

  @Builder.Default
  @ManyToMany
  @JoinTable(name = "ennemi_capacite", joinColumns = @JoinColumn(name = "ennemi_id"), inverseJoinColumns = @JoinColumn(name = "capacite_id"))
  private Set<Capacite> capacites = new HashSet<>();
}