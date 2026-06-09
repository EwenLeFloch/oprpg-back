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

  @Column(nullable = false, unique = true)
  private String imagePath;

  @Column()
  private String description;

  @Builder.Default
  @Min(1)
  @Column(nullable = false)
  private int niveauRequis = 1;
}
