package com.onepiecerpg.api.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "utilisateur")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Utilisateur {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String pseudo;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String motDePasseHash;

  @Builder.Default
  @Column(nullable = false)
  private String role = "USER";

  @Builder.Default
  @Column(nullable = false, updatable = false)
  private LocalDateTime dateCreation = LocalDateTime.now();
}