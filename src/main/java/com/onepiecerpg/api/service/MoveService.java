package com.onepiecerpg.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onepiecerpg.api.entity.Move;
import com.onepiecerpg.api.entity.TypeMove;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.MoveRepository;

@Service
public class MoveService {

  private final MoveRepository moveRepository;

  public MoveService(MoveRepository moveRepository) {
    this.moveRepository = moveRepository;
  }

  public List<Move> recupererTousLesMoves() {
    return moveRepository.findAll();
  }

  public Move recupererMoveParId(Long moveId) {
    return moveRepository.findById(moveId)
        .orElseThrow(() -> new RessourceIntrouvableException("Move introuvable"));
  }

  public Move recupererMoveParNom(String nom) {
    return moveRepository.findByNom(nom)
        .orElseThrow(() -> new RessourceIntrouvableException("Move introuvable"));
  }

  public List<Move> recupererMovesParType(TypeMove typeMove) {
    return moveRepository.findByTypeMove(typeMove);
  }
}