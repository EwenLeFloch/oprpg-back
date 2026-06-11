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

import com.onepiecerpg.api.entity.Capacite;
import com.onepiecerpg.api.entity.TypeCapacite;
import com.onepiecerpg.api.exception.GlobalExceptionHandler;
import com.onepiecerpg.api.security.JwtAuthenticationFilter;
import com.onepiecerpg.api.service.JwtService;
import com.onepiecerpg.api.service.CapaciteService;

@WebMvcTest(controllers = CapaciteController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class
})
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class CapaciteControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CapaciteService capaciteService;

  @MockitoBean
  private Clock clock;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("Doit retourner tous les capacites")
  void shouldGetAllCapacites() throws Exception {
    when(capaciteService.recupererTousLesCapacites()).thenReturn(List.of(capacite()));

    mockMvc.perform(get("/api/capacites"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].nom").value("Coup de poing"))
        .andExpect(jsonPath("$[0].typeCapacite").value("ATTAQUE"));
  }

  @Test
  @DisplayName("Doit retourner les capacites du personnage connecté")
  void shouldGetConnectedCharacterCapacites() throws Exception {
    when(capaciteService.recupererCapacitesPersonnageConnecte()).thenReturn(List.of(capacite()));

    mockMvc.perform(get("/api/capacites/personnage"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].nom").value("Coup de poing"))
        .andExpect(jsonPath("$[0].typeCapacite").value("ATTAQUE"));
  }

  @Test
  @DisplayName("Doit retourner un capacite par id")
  void shouldGetCapaciteById() throws Exception {
    when(capaciteService.recupererCapaciteParId(1L)).thenReturn(capacite());

    mockMvc.perform(get("/api/capacites/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.nom").value("Coup de poing"))
        .andExpect(jsonPath("$.typeCapacite").value("ATTAQUE"));
  }

  @Test
  @DisplayName("Doit retourner les capacites par type")
  void shouldGetCapacitesByType() throws Exception {
    when(capaciteService.recupererCapacitesParType(TypeCapacite.ATTAQUE)).thenReturn(List.of(capacite()));

    mockMvc.perform(get("/api/capacites/type/ATTAQUE"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].nom").value("Coup de poing"))
        .andExpect(jsonPath("$[0].typeCapacite").value("ATTAQUE"));
  }

  private Capacite capacite() {
    Capacite capacite = new Capacite();
    capacite.setId(1L);
    capacite.setNom("Coup de poing");
    capacite.setDescription("Une attaque simple au corps-à-corps.");
    capacite.setTypeCapacite(TypeCapacite.ATTAQUE);
    capacite.setValeurMin(4);
    capacite.setValeurMax(7);
    capacite.setDuree(1);
    capacite.setPrecision(95);
    capacite.setCoutEndurance(1);
    return capacite;
  }
}