package com.onepiecerpg.api.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onepiecerpg.api.entity.Ile;

public interface IleRepository extends JpaRepository<Ile, Long> {
    Optional<Ile> findByNom(String nom);
}
