package com.onepiecerpg.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.onepiecerpg.api.dto.ConnexionResponse;
import com.onepiecerpg.api.security.JwtAuthenticationFilter;
import com.onepiecerpg.api.service.UtilisateurService;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UtilisateurService utilisateurService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("Doit retourner 200 lors d'une connexion valide avec email")
  void shouldLoginUserWithEmail() throws Exception {
    ConnexionResponse response = new ConnexionResponse(
        "mocked-token",
        "Bearer",
        "testuser",
        "USER");

    when(utilisateurService.connecterUtilisateur(ArgumentMatchers.any())).thenReturn(response);

    String body = """
        {
          "identifiant": "test@test.com",
          "motDePasse": "Password123"
        }
        """;

    mockMvc.perform(post("/api/auth/connexion")
        .contentType("application/json")
        .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").value("mocked-token"))
        .andExpect(jsonPath("$.type").value("Bearer"))
        .andExpect(jsonPath("$.pseudo").value("testuser"))
        .andExpect(jsonPath("$.role").value("USER"));
  }

  @Test
  @DisplayName("Doit retourner 200 lors d'une connexion valide avec pseudo")
  void shouldLoginUserWithPseudo() throws Exception {
    ConnexionResponse response = new ConnexionResponse(
        "mocked-token",
        "Bearer",
        "testuser",
        "USER");

    when(utilisateurService.connecterUtilisateur(ArgumentMatchers.any())).thenReturn(response);

    String body = """
        {
          "identifiant": "testuser",
          "motDePasse": "Password123"
        }
        """;

    mockMvc.perform(post("/api/auth/connexion")
        .contentType("application/json")
        .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.pseudo").value("testuser"));
  }

  @Test
  @DisplayName("Doit retourner 400 lors d'une connexion avec identifiant vide")
  void shouldReturnBadRequestWhenLoginInvalid() throws Exception {
    String body = """
        {
          "identifiant": "",
          "motDePasse": ""
        }
        """;

    mockMvc.perform(post("/api/auth/connexion")
        .contentType("application/json")
        .content(body))
        .andExpect(status().isBadRequest());
  }
}