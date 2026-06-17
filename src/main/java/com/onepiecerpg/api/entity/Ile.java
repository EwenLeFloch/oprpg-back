package com.onepiecerpg.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "ile")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String nom;

  @Column(name = "nom_image", nullable = false, unique = true)
  private String nomImage;

  @Column()
  private String description;

  @Builder.Default
  @Min(1)
  @Column(nullable = false)
  private int niveauRequis = 1;

  @Column(name = "position_x", nullable = false)
  private int positionX;

  @Column(name = "position_y", nullable = false)
  private int positionY;
}