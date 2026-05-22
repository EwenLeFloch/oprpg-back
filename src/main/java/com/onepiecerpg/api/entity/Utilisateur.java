package com.onepiecerpg.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "utilisateur")
@Getter
@Setter
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

    @Column(nullable = false)
    private String role = "USER";
}