package com.onepiecerpg.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onepiecerpg.api.entity.News;

public interface NewsRepository extends JpaRepository<News, Long> {
    
}
