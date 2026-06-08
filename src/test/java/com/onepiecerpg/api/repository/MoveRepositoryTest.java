package com.onepiecerpg.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.onepiecerpg.api.entity.Move;
import com.onepiecerpg.api.entity.TypeMove;

@DataJpaTest
class MoveRepositoryTest {

    @Autowired
    private MoveRepository moveRepository;

    @Test
    void shouldFindByNom() {
        Move move = move("Coup de poing", TypeMove.ATTAQUE);

        moveRepository.save(move);

        Optional<Move> result = moveRepository.findByNom("Coup de poing");

        assertThat(result).isPresent();
        assertThat(result.get().getTypeMove()).isEqualTo(TypeMove.ATTAQUE);
    }

    @Test
    void shouldFindByTypeMove() {
        moveRepository.save(move("Coup de poing", TypeMove.ATTAQUE));
        moveRepository.save(move("Lait", TypeMove.SOIN));

        List<Move> result = moveRepository.findByTypeMove(TypeMove.ATTAQUE);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getNom()).isEqualTo("Coup de poing");
    }

    private Move move(String nom, TypeMove typeMove) {
        Move move = new Move();
        move.setNom(nom);
        move.setDescription("Description test");
        move.setTypeMove(typeMove);
        move.setValeurMin(1);
        move.setValeurMax(3);
        move.setDuree(1);
        move.setPrecision(100);
        move.setCoutEndurance(1);
        return move;
    }
}