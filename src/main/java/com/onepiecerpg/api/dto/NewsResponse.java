package com.onepiecerpg.api.dto;

import java.time.LocalDateTime;

import com.onepiecerpg.api.entity.News;

public record NewsResponse(
        Long id,
        String titre,
        String contenu,
        LocalDateTime dateCreation
) {
    public static NewsResponse from(News news) {
        return new NewsResponse(
                news.getId(),
                news.getTitre(),
                news.getContenu(),
                news.getDateCreation()
        );
    }
}