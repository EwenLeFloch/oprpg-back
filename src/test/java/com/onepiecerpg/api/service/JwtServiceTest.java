package com.onepiecerpg.api.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.onepiecerpg.api.entity.Utilisateur;

class JwtServiceTest {
  private JwtService jwtService;

  @BeforeEach
  void setUp() {
    jwtService = new JwtService();

    ReflectionTestUtils.setField(jwtService, "secret",
        "0123456789012345678901234567890101234567890123456789012345678901");
    ReflectionTestUtils.setField(jwtService, "expiration", 3600000L);
  }

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