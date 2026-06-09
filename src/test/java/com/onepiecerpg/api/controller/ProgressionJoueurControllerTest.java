package com.onepiecerpg.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.onepiecerpg.api.config.ClockConfig;
import com.onepiecerpg.api.dto.ProgressionJoueurResponse;
import com.onepiecerpg.api.exception.GlobalExceptionHandler;
import com.onepiecerpg.api.security.JwtAuthenticationFilter;
import com.onepiecerpg.api.service.JwtService;
import com.onepiecerpg.api.service.ProgressionJoueurService;

@WebMvcTest(controllers = ProgressionJoueurController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class
})
@Import({
    GlobalExceptionHandler.class,
    ClockConfig.class
})
@AutoConfigureMockMvc(addFilters = false)
class ProgressionJoueurControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ProgressionJoueurService progressionJoueurService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("Doit retourner la progression du joueur connecté")
  void shouldGetConnectedProgression() throws Exception {
    ProgressionJoueurResponse response = progressionResponse(null);

    when(progressionJoueurService.getProgressionConnectee()).thenReturn(response);

    mockMvc.perform(get("/api/progression/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.niveau").value(1))
        .andExpect(jsonPath("$.experience").value(0))
        .andExpect(jsonPath("$.personnage").value("Luffy"))
        .andExpect(jsonPath("$.faction").doesNotExist());
  }

  @Test
  @DisplayName("Doit choisir une faction")
  void shouldChooseFaction() throws Exception {
    ProgressionJoueurResponse response = progressionResponse("Pirate");

    when(progressionJoueurService.choisirFaction(1L)).thenReturn(response);

    mockMvc.perform(post("/api/progression/faction/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.faction").value("Pirate"));
  }

  @Test
  @DisplayName("Doit retourner 409 si la faction est déjà choisie")
  void shouldReturnConflictWhenFactionAlreadyChosen() throws Exception {
    when(progressionJoueurService.choisirFaction(1L))
        .thenThrow(new IllegalStateException("La faction a déjà été choisie"));

    mockMvc.perform(post("/api/progression/faction/1"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("La faction a déjà été choisie"));
  }

  private ProgressionJoueurResponse progressionResponse(String faction) {
    return new ProgressionJoueurResponse(
        1L,
        1,
        0,
        10,
        10,
        1,
        10,
        10,
        0,
        0L,
        "Luffy",
        faction);
  }
}