package com.dreamteam.educa.repositories;

import com.dreamteam.educa.entities.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    Article findByFilename(String filename);

    Optional<Article> findArticleByTitle(String title);

}
