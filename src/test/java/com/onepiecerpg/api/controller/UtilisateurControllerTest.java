package com.onepiecerpg.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.onepiecerpg.api.dto.UtilisateurResponseDto;
import com.onepiecerpg.api.security.JwtAuthenticationFilter;
import com.onepiecerpg.api.service.JwtService;
import com.onepiecerpg.api.service.UtilisateurService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;

@WebMvcTest(controllers = UtilisateurController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
class UtilisateurControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private UtilisateurService utilisateurService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @MockitoBean
  private Clock clock;

  @Test
  @DisplayName("Doit retourner les informations de l'utilisateur connecté")
  void shouldReturnCurrentUser() throws Exception {
    UtilisateurResponseDto response = new UtilisateurResponseDto(
        1L,
        "testuser",
        "test@test.com",
        "USER");

    when(utilisateurService.recupererUtilisateurConnecte()).thenReturn(response);

    mockMvc.perform(get("/api/utilisateurs/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.pseudo").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@test.com"))
        .andExpect(jsonPath("$.role").value("USER"));
  }
}
