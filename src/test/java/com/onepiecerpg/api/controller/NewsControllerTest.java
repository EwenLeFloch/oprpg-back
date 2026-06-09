package com.onepiecerpg.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.Month;
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

import com.onepiecerpg.api.config.ClockConfig;
import com.onepiecerpg.api.dto.NewsResponse;
import com.onepiecerpg.api.exception.GlobalExceptionHandler;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.security.JwtAuthenticationFilter;
import com.onepiecerpg.api.service.JwtService;
import com.onepiecerpg.api.service.NewsService;

@WebMvcTest(controllers = NewsController.class, excludeAutoConfiguration = {
    SecurityAutoConfiguration.class,
    SecurityFilterAutoConfiguration.class
})
@Import({
    GlobalExceptionHandler.class,
    ClockConfig.class
})
@AutoConfigureMockMvc(addFilters = false)
class NewsControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private NewsService newsService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("Doit retourner toutes les news")
  void shouldGetAllNews() throws Exception {
    NewsResponse news = new NewsResponse(
        1L,
        "Bienvenue",
        "Bienvenue sur One Piece RPG",
        LocalDateTime.of(2026, Month.JUNE, 8, 10, 0));

    when(newsService.recupererToutesLesNews()).thenReturn(List.of(news));

    mockMvc.perform(get("/api/news"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].titre").value("Bienvenue"))
        .andExpect(jsonPath("$[0].contenu").value("Bienvenue sur One Piece RPG"));
  }

  @Test
  @DisplayName("Doit retourner une news par id")
  void shouldGetNewsById() throws Exception {
    NewsResponse news = new NewsResponse(
        1L,
        "Patch note",
        "Ajout du système de combat",
        LocalDateTime.of(2026, Month.JUNE, 8, 10, 0));

    when(newsService.recupererNewsParId(1L)).thenReturn(news);

    mockMvc.perform(get("/api/news/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.titre").value("Patch note"))
        .andExpect(jsonPath("$.contenu").value("Ajout du système de combat"));
  }

  @Test
  @DisplayName("Doit retourner 404 si la news est introuvable")
  void shouldReturnNotFoundWhenNewsDoesNotExist() throws Exception {
    when(newsService.recupererNewsParId(1L))
        .thenThrow(new RessourceIntrouvableException("News introuvable"));

    mockMvc.perform(get("/api/news/1"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("News introuvable"));
  }

  @Test
  @DisplayName("Doit créer une news")
  void shouldCreateNews() throws Exception {
    NewsResponse response = new NewsResponse(
        1L,
        "Nouvelle île",
        "Dawn Island est disponible",
        LocalDateTime.of(2026, Month.JUNE, 8, 10, 0));

    when(newsService.creerNews(any())).thenReturn(response);

    String body = """
        {
          "titre": "Nouvelle île",
          "contenu": "Dawn Island est disponible"
        }
        """;

    mockMvc.perform(post("/api/news")
        .contentType("application/json")
        .content(body))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.titre").value("Nouvelle île"))
        .andExpect(jsonPath("$.contenu").value("Dawn Island est disponible"));
  }

  @Test
  @DisplayName("Doit retourner 400 lors de la création d'une news invalide")
  void shouldReturnBadRequestWhenCreatingInvalidNews() throws Exception {
    String body = """
        {
          "titre": "",
          "contenu": ""
        }
        """;

    mockMvc.perform(post("/api/news")
        .contentType("application/json")
        .content(body))
        .andExpect(status().isBadRequest());
  }

  @Test
  @DisplayName("Doit modifier une news")
  void shouldUpdateNews() throws Exception {
    NewsResponse response = new NewsResponse(
        1L,
        "Titre modifié",
        "Contenu modifié",
        LocalDateTime.of(2026, Month.JUNE, 8, 10, 0));

    when(newsService.modifierNews(eq(1L), any())).thenReturn(response);

    String body = """
        {
          "titre": "Titre modifié",
          "contenu": "Contenu modifié"
        }
        """;

    mockMvc.perform(put("/api/news/1")
        .contentType("application/json")
        .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.titre").value("Titre modifié"))
        .andExpect(jsonPath("$.contenu").value("Contenu modifié"));
  }

  @Test
  @DisplayName("Doit supprimer une news")
  void shouldDeleteNews() throws Exception {
    doNothing().when(newsService).supprimerNews(1L);

    mockMvc.perform(delete("/api/news/1"))
        .andExpect(status().isNoContent());

    verify(newsService).supprimerNews(1L);
  }
}