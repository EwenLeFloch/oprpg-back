package com.onepiecerpg.api.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.onepiecerpg.api.entity.Utilisateur;

@DataJpaTest
class UtilisateurRepositoryTest {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Test
    void shouldFindByEmail() {
        Utilisateur utilisateur = utilisateur();
        utilisateurRepository.save(utilisateur);

        Optional<Utilisateur> result = utilisateurRepository.findByEmail("luffy@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getPseudo()).isEqualTo("luffy");
    }

    @Test
    void shouldFindByPseudo() {
        Utilisateur utilisateur = utilisateur();
        utilisateurRepository.save(utilisateur);

        Optional<Utilisateur> result = utilisateurRepository.findByPseudo("luffy");

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("luffy@test.com");
    }

    private Utilisateur utilisateur() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setPseudo("luffy");
        utilisateur.setEmail("luffy@test.com");
        utilisateur.setMotDePasseHash("hashed-password");
        utilisateur.setRole("USER");
        return utilisateur;
    }
}