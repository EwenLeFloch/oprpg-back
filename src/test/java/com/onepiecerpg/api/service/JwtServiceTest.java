package com.onepiecerpg.api.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.onepiecerpg.api.entity.Utilisateur;

@SpringBootTest(properties = {
    "jwt.secret=monSecret",
    "jwt.expiration=3600000"
})
class JwtServiceTest {
  private JwtService jwtService;

  @Test
  void genererToken_shouldCreateValidToken() {
    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setEmail("test@test.com");

    String token = jwtService.genererToken(utilisateur);
    assertNotNull(token);
    assertFalse(token.isBlank());
  }

  @Test
  void extraireEmail_shouldReturnEmailFromToken() {
    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setEmail("test@test.com");
    
    String token = jwtService.genererToken(utilisateur);
    String email = jwtService.extraireEmail(token);
    assertEquals("test@test.com", email);
  }

  @Test
  void estTokenValide_shouldReturnTrue_whenTokenMatchesUtilisateur() {
    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setEmail("test@test.com");

    String token = jwtService.genererToken(utilisateur);
    boolean resultat = jwtService.estTokenValide(token);

    assertTrue(resultat);
  }
}