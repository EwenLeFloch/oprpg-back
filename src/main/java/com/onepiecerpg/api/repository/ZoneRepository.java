package com.onepiecerpg.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onepiecerpg.api.entity.Zone;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    List<Zone> findByIleId(Long ileId);

    Optional<Zone> findByNom(String nom);
}
