package com.onepiecerpg.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onepiecerpg.api.dto.NewsRequest;
import com.onepiecerpg.api.dto.NewsResponse;
import com.onepiecerpg.api.entity.News;
import com.onepiecerpg.api.repository.NewsRepository;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public List<NewsResponse> recupererToutesLesNews() {
        return newsRepository.findAll()
                .stream()
                .map(NewsResponse::from)
                .toList();
    }

    public NewsResponse recupererNewsParId(Long newsId) {
        return NewsResponse.from(recupererNews(newsId));
    }

    public NewsResponse creerNews(NewsRequest request) {
        News news = new News();
        news.setTitre(request.titre());
        news.setContenu(request.contenu());

        return NewsResponse.from(newsRepository.save(news));
    }

    public NewsResponse modifierNews(Long newsId, NewsRequest request) {
        News news = recupererNews(newsId);

        news.setTitre(request.titre());
        news.setContenu(request.contenu());

        return NewsResponse.from(newsRepository.save(news));
    }

    public void supprimerNews(Long newsId) {
        if (!newsRepository.existsById(newsId)) {
            throw new RuntimeException("News introuvable");
        }

        newsRepository.deleteById(newsId);
    }

    private News recupererNews(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News introuvable"));
    }
}