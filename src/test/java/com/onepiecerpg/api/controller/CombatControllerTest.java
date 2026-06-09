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
import com.onepiecerpg.api.dto.CombatResponse;
import com.onepiecerpg.api.entity.StatutCombat;
import com.onepiecerpg.api.exception.GlobalExceptionHandler;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.security.JwtAuthenticationFilter;
import com.onepiecerpg.api.service.CombatService;
import com.onepiecerpg.api.service.JwtService;

@WebMvcTest(controllers = CombatController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class
})
@Import({
    GlobalExceptionHandler.class,
    ClockConfig.class
})
@AutoConfigureMockMvc(addFilters = false)
class CombatControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CombatService combatService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("Doit démarrer un combat")
  void shouldStartCombat() throws Exception {
    CombatResponse response = combatResponse(StatutCombat.EN_COURS);

    when(combatService.demarrerCombat(1L)).thenReturn(response);

    mockMvc.perform(post("/api/combats/ennemis/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.combatId").value(1))
        .andExpect(jsonPath("$.ennemi").value("Bandit"))
        .andExpect(jsonPath("$.vieEnnemiActuelle").value(20))
        .andExpect(jsonPath("$.vieJoueurActuelle").value(10))
        .andExpect(jsonPath("$.statut").value("EN_COURS"));
  }

  @Test
  @DisplayName("Doit retourner le combat en cours")
  void shouldGetCurrentCombat() throws Exception {
    CombatResponse response = combatResponse(StatutCombat.EN_COURS);

    when(combatService.recupererCombatEnCours()).thenReturn(response);

    mockMvc.perform(get("/api/combats/en-cours"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("EN_COURS"));
  }

  @Test
  @DisplayName("Doit utiliser un move")
  void shouldUseMove() throws Exception {
    CombatResponse response = new CombatResponse(
        1L,
        "Bandit",
        12,
        8,
        StatutCombat.EN_COURS);

    when(combatService.utiliserMove(1L)).thenReturn(response);

    mockMvc.perform(post("/api/combats/moves/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vieEnnemiActuelle").value(12))
        .andExpect(jsonPath("$.vieJoueurActuelle").value(8));
  }

  @Test
  @DisplayName("Doit fuir un combat")
  void shouldFleeCombat() throws Exception {
    CombatResponse response = combatResponse(StatutCombat.FUITE);

    when(combatService.fuirCombat()).thenReturn(response);

    mockMvc.perform(post("/api/combats/fuite"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.statut").value("FUITE"));
  }

  @Test
  @DisplayName("Doit retourner 404 si aucun combat en cours")
  void shouldReturnNotFoundWhenNoCurrentCombat() throws Exception {
    when(combatService.recupererCombatEnCours())
        .thenThrow(new RessourceIntrouvableException("Aucun combat en cours"));

    mockMvc.perform(get("/api/combats/en-cours"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Aucun combat en cours"));
  }

  private CombatResponse combatResponse(StatutCombat statut) {
    return new CombatResponse(
        1L,
        "Bandit",
        20,
        10,
        statut);
  }
}