package com.onepiecerpg.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onepiecerpg.api.entity.Capacite;
import com.onepiecerpg.api.entity.TypeCapacite;

public interface CapaciteRepository extends JpaRepository<Capacite, Long> {
  Optional<Capacite> findByNom(String nom);

  List<Capacite> findByTypeCapacite(TypeCapacite typeCapacite);
}
