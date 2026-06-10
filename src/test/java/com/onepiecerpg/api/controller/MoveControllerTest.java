package com.onepiecerpg.api.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.Clock;
import java.util.List;

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

import com.onepiecerpg.api.entity.Move;
import com.onepiecerpg.api.entity.TypeMove;
import com.onepiecerpg.api.exception.GlobalExceptionHandler;
import com.onepiecerpg.api.security.JwtAuthenticationFilter;
import com.onepiecerpg.api.service.JwtService;
import com.onepiecerpg.api.service.MoveService;

@WebMvcTest(controllers = MoveController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class
})
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class MoveControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private MoveService moveService;

  @MockitoBean
  private Clock clock;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("Doit retourner tous les moves")
  void shouldGetAllMoves() throws Exception {
    when(moveService.recupererTousLesMoves()).thenReturn(List.of(move()));

    mockMvc.perform(get("/api/moves"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].nom").value("Coup de poing"))
        .andExpect(jsonPath("$[0].typeMove").value("ATTAQUE"));
  }

  @Test
  @DisplayName("Doit retourner les moves du personnage connecté")
  void shouldGetConnectedCharacterMoves() throws Exception {
    when(moveService.recupererMovesPersonnageConnecte()).thenReturn(List.of(move()));

    mockMvc.perform(get("/api/moves/personnage"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].nom").value("Coup de poing"))
        .andExpect(jsonPath("$[0].typeMove").value("ATTAQUE"));
  }

  @Test
  @DisplayName("Doit retourner un move par id")
  void shouldGetMoveById() throws Exception {
    when(moveService.recupererMoveParId(1L)).thenReturn(move());

    mockMvc.perform(get("/api/moves/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.nom").value("Coup de poing"))
        .andExpect(jsonPath("$.typeMove").value("ATTAQUE"));
  }

  @Test
  @DisplayName("Doit retourner les moves par type")
  void shouldGetMovesByType() throws Exception {
    when(moveService.recupererMovesParType(TypeMove.ATTAQUE)).thenReturn(List.of(move()));

    mockMvc.perform(get("/api/moves/type/ATTAQUE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].nom").value("Coup de poing"))
        .andExpect(jsonPath("$[0].typeMove").value("ATTAQUE"));
  }

  private Move move() {
    Move move = new Move();
    move.setId(1L);
    move.setNom("Coup de poing");
    move.setDescription("Une attaque simple au corps-à-corps.");
    move.setTypeMove(TypeMove.ATTAQUE);
    move.setValeurMin(4);
    move.setValeurMax(7);
    move.setDuree(1);
    move.setPrecision(95);
    move.setCoutEndurance(1);
    return move;
  }
}