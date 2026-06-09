package com.onepiecerpg.api.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "faction")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Faction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String nom;

  private String description;
}
