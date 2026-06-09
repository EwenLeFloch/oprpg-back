package com.onepiecerpg.api.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onepiecerpg.api.entity.Move;
import com.onepiecerpg.api.entity.TypeMove;

public interface MoveRepository extends JpaRepository<Move, Long> {
  Optional<Move> findByNom(String nom);

  List<Move> findByTypeMove(TypeMove typeMove);
}
