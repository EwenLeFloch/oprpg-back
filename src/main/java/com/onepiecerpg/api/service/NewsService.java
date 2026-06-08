package com.onepiecerpg.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onepiecerpg.api.entity.News;
import com.onepiecerpg.api.repository.NewsRepository;

@Service
public class NewsService {

    private final NewsRepository newsRepository;

    public NewsService(NewsRepository newsRepository) {
        this.newsRepository = newsRepository;
    }

    public List<News> recupererToutesLesNews() {
        return newsRepository.findAll();
    }

    public News recupererNewsParId(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News introuvable"));
    }

    public News creerNews(News news) {
        return newsRepository.save(news);
    }

    public News modifierNews(Long newsId, News newsModifiee) {
        News news = recupererNewsParId(newsId);

        news.setTitre(newsModifiee.getTitre());
        news.setContenu(newsModifiee.getContenu());

        return newsRepository.save(news);
    }

    public void supprimerNews(Long newsId) {
        if (!newsRepository.existsById(newsId)) {
            throw new RuntimeException("News introuvable");
        }

        newsRepository.deleteById(newsId);
    }
}