package com.onepiecerpg.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "zone")
@Getter
@Setter
public class Zone {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Min(1)
    @Column(nullable = false)
    private int niveauRequis = 1;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ile_id", nullable = false)
    private Ile ile;
}
