package com.onepiecerpg.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.onepiecerpg.api.entity.Utilisateur;
import java.util.Optional;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {
    Optional<Utilisateur> findByUsername(String username);
    Optional<Utilisateur> findByMail(String mail);
}
