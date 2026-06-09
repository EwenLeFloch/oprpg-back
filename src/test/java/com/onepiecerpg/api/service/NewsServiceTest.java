package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.onepiecerpg.api.dto.NewsRequest;
import com.onepiecerpg.api.dto.NewsResponse;
import com.onepiecerpg.api.entity.News;
import com.onepiecerpg.api.exception.RessourceIntrouvableException;
import com.onepiecerpg.api.repository.NewsRepository;

class NewsServiceTest {

  private NewsRepository newsRepository;
  private NewsService newsService;
  private Clock clock;

  @BeforeEach
  void setUp() {
    newsRepository = mock(NewsRepository.class);
    ZoneId zone = ZoneId.of("Europe/Paris");
    clock = Clock.fixed(LocalDateTime.of(2026, Month.JUNE, 8, 10, 0).atZone(zone).toInstant(), zone);
    newsService = new NewsService(newsRepository, clock);
  }

  @Test
  void shouldGetAllNews() {
    News news = news("Bienvenue", "Bienvenue sur One Piece RPG");

    when(newsRepository.findAll()).thenReturn(List.of(news));

    List<NewsResponse> result = newsService.recupererToutesLesNews();

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().titre()).isEqualTo("Bienvenue");
  }

  @Test
  void shouldGetNewsById() {
    News news = news("Patch note", "Ajout du système de combat");

    when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

    NewsResponse result = newsService.recupererNewsParId(1L);

    assertThat(result.titre()).isEqualTo("Patch note");
    assertThat(result.contenu()).isEqualTo("Ajout du système de combat");
  }

  @Test
  void shouldThrowWhenNewsNotFoundById() {
    when(newsRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> newsService.recupererNewsParId(1L))
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("News introuvable");
  }

  @Test
  void shouldCreateNews() {
    NewsRequest request = new NewsRequest(
        "Nouvelle île",
        "Dawn Island est disponible");

    when(newsRepository.save(any(News.class))).thenAnswer(invocation -> {
      News news = invocation.getArgument(0);
      news.setId(1L);
      return news;
    });

    NewsResponse result = newsService.creerNews(request);

    assertThat(result.id()).isEqualTo(1L);
    assertThat(result.titre()).isEqualTo("Nouvelle île");
    assertThat(result.contenu()).isEqualTo("Dawn Island est disponible");
    assertThat(result.dateCreation()).isBeforeOrEqualTo(LocalDateTime.now(clock));
    verify(newsRepository).save(any(News.class));
  }

  @Test
  void shouldUpdateNews() {
    News existingNews = news("Ancien titre", "Ancien contenu");
    NewsRequest request = new NewsRequest(
        "Nouveau titre",
        "Nouveau contenu");

    when(newsRepository.findById(1L)).thenReturn(Optional.of(existingNews));
    when(newsRepository.save(existingNews)).thenReturn(existingNews);

    NewsResponse result = newsService.modifierNews(1L, request);

    assertThat(result.titre()).isEqualTo("Nouveau titre");
    assertThat(result.contenu()).isEqualTo("Nouveau contenu");

    verify(newsRepository).save(existingNews);
  }

  @Test
  void shouldThrowWhenUpdatingUnknownNews() {
    NewsRequest request = new NewsRequest(
        "Nouveau titre",
        "Nouveau contenu");

    when(newsRepository.findById(1L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> newsService.modifierNews(1L, request))
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("News introuvable");

    verify(newsRepository, never()).save(any());
  }

  @Test
  void shouldDeleteNews() {
    when(newsRepository.existsById(1L)).thenReturn(true);

    newsService.supprimerNews(1L);

    verify(newsRepository).deleteById(1L);
  }

  @Test
  void shouldThrowWhenDeletingUnknownNews() {
    when(newsRepository.existsById(1L)).thenReturn(false);

    assertThatThrownBy(() -> newsService.supprimerNews(1L))
        .isInstanceOf(RessourceIntrouvableException.class)
        .hasMessage("News introuvable");

    verify(newsRepository, never()).deleteById(anyLong());
  }

  private News news(String titre, String contenu) {
    News news = new News();
    news.setId(1L);
    news.setTitre(titre);
    news.setContenu(contenu);
    news.setDateCreation(LocalDateTime.of(2026, Month.JUNE, 8, 10, 0));
    return news;
  }
}