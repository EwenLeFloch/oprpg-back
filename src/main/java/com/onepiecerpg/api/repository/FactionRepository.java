package com.onepiecerpg.api.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.onepiecerpg.api.entity.Faction;

public interface FactionRepository extends JpaRepository<Faction, Long> {
    Optional<Faction> findByNom(String nom);
}
