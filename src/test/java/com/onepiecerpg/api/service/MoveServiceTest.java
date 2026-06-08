package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.onepiecerpg.api.entity.Move;
import com.onepiecerpg.api.entity.TypeMove;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.MoveRepository;

class MoveServiceTest {

    private MoveRepository moveRepository;
    private MoveService moveService;

    @BeforeEach
    void setUp() {
        moveRepository = mock(MoveRepository.class);
        moveService = new MoveService(moveRepository);
    }

    @Test
    void shouldGetAllMoves() {
        when(moveRepository.findAll()).thenReturn(List.of(new Move()));

        assertThat(moveService.recupererTousLesMoves()).hasSize(1);
    }

    @Test
    void shouldGetMoveById() {
        Move move = new Move();
        move.setId(1L);
        move.setNom("Coup de poing");

        when(moveRepository.findById(1L)).thenReturn(Optional.of(move));

        assertThat(moveService.recupererMoveParId(1L).getNom()).isEqualTo("Coup de poing");
    }

    @Test
    void shouldGetMovesByType() {
        when(moveRepository.findByTypeMove(TypeMove.ATTAQUE)).thenReturn(List.of(new Move()));

        assertThat(moveService.recupererMovesParType(TypeMove.ATTAQUE)).hasSize(1);
    }

    @Test
    void shouldThrowWhenMoveNotFound() {
        when(moveRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> moveService.recupererMoveParId(1L))
                .isInstanceOf(RessourceIntrouvableException.class)
                .hasMessage("Move introuvable");
    }
}