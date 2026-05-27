package com.onepiecerpg.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.onepiecerpg.api.entity.Utilisateur;
import com.onepiecerpg.api.service.UtilisateurService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UtilisateurService utilisateurService;

  @Test
  @DisplayName("Doit retourner 201 lors d'une inscription valide")
  void shouldRegisterUser() throws Exception {

    Utilisateur utilisateur = new Utilisateur();
    utilisateur.setId(1L);
    utilisateur.setPseudo("testuser");
    utilisateur.setEmail("test@test.com");
    utilisateur.setMotDePasseHash("hashedpassword");
    utilisateur.setRole("USER");

    when(utilisateurService.creerUtilisateur(ArgumentMatchers.any())).thenReturn(utilisateur);

    String body = """
        {
          "pseudo": "testuser",
          "email": "test@test.com",
          "motDePasse": "Password123"
        }
        """;

    mockMvc.perform(post("/api/auth/inscription")
        .contentType("application/json")
        .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.pseudo").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@test.com"));
  }
}
