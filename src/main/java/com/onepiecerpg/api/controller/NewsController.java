package com.onepiecerpg.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onepiecerpg.api.dto.NewsRequest;
import com.onepiecerpg.api.dto.NewsResponse;
import com.onepiecerpg.api.service.NewsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    private final NewsService newsService;

    public NewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public ResponseEntity<List<NewsResponse>> recupererToutesLesNews() {
        return ResponseEntity.ok(newsService.recupererToutesLesNews());
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<NewsResponse> recupererNewsParId(@PathVariable Long newsId) {
        return ResponseEntity.ok(newsService.recupererNewsParId(newsId));
    }

    @PostMapping
    public ResponseEntity<NewsResponse> creerNews(@Valid @RequestBody NewsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(newsService.creerNews(request));
    }

    @PutMapping("/{newsId}")
    public ResponseEntity<NewsResponse> modifierNews(
        @PathVariable Long newsId,
        @Valid @RequestBody NewsRequest request
    ) {
        return ResponseEntity.ok(newsService.modifierNews(newsId, request));
    }

    @DeleteMapping("/{newsId}")
    public ResponseEntity<Void> supprimerNews(@PathVariable Long newsId) {
        newsService.supprimerNews(newsId);
        return ResponseEntity.noContent().build();
    }
}