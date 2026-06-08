package com.onepiecerpg.api.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.onepiecerpg.api.dto.NewsResponse;
import com.onepiecerpg.api.entity.News;
import com.onepiecerpg.api.repository.NewsRepository;

class NewsServiceTest {

    private NewsRepository newsRepository;
    private NewsService newsService;

    @BeforeEach
    void setUp() {
        newsRepository = mock(NewsRepository.class);
        newsService = new NewsService(newsRepository);
    }

    @Test
    void shouldGetAllNews() {
        News news = news("Bienvenue", "Bienvenue sur One Piece RPG");

        when(newsRepository.findAll()).thenReturn(List.of(news));

        List<NewsResponse> result = newsService.recupererToutesLesNews();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTitre()).isEqualTo("Bienvenue");
    }

    @Test
    void shouldGetNewsById() {
        News news = news("Patch note", "Ajout du système de combat");

        when(newsRepository.findById(1L)).thenReturn(Optional.of(news));

        News result = newsService.recupererNewsParId(1L);

        assertThat(result.getTitre()).isEqualTo("Patch note");
        assertThat(result.getContenu()).isEqualTo("Ajout du système de combat");
    }

    @Test
    void shouldThrowWhenNewsNotFoundById() {
        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.recupererNewsParId(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("News introuvable");
    }

    @Test
    void shouldCreateNews() {
        News news = news("Nouvelle île", "Dawn Island est disponible");

        when(newsRepository.save(news)).thenReturn(news);

        News result = newsService.creerNews(news);

        assertThat(result.getTitre()).isEqualTo("Nouvelle île");
        verify(newsRepository).save(news);
    }

    @Test
    void shouldUpdateNews() {
        News existingNews = news("Ancien titre", "Ancien contenu");
        News updatedNews = news("Nouveau titre", "Nouveau contenu");

        when(newsRepository.findById(1L)).thenReturn(Optional.of(existingNews));
        when(newsRepository.save(existingNews)).thenReturn(existingNews);

        News result = newsService.modifierNews(1L, updatedNews);

        assertThat(result.getTitre()).isEqualTo("Nouveau titre");
        assertThat(result.getContenu()).isEqualTo("Nouveau contenu");

        verify(newsRepository).save(existingNews);
    }

    @Test
    void shouldThrowWhenUpdatingUnknownNews() {
        News updatedNews = news("Nouveau titre", "Nouveau contenu");

        when(newsRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> newsService.modifierNews(1L, updatedNews))
                .isInstanceOf(RuntimeException.class)
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
                .isInstanceOf(RuntimeException.class)
                .hasMessage("News introuvable");

        verify(newsRepository, never()).deleteById(anyLong());
    }

    private News news(String titre, String contenu) {
        News news = new News();
        news.setId(1L);
        news.setTitre(titre);
        news.setContenu(contenu);
        return news;
    }
}