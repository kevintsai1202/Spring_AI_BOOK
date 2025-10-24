package com.example.controller;

import com.example.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 結構化輸出控制器
 * 使用 ChatClient entity() 方法實現現代化的結構化輸出
 * 對應 5.10 章節的結構化資料轉換器實現
 */
@RestController
@RequestMapping("/api/structured")
@RequiredArgsConstructor
@Slf4j
public class StructuredOutputController {

    private final ChatModel chatModel;

    /**
     * 單一演員的電影作品
     * @param actor 演員名稱
     * @return 演員電影作品
     */
    @GetMapping("/actor-films")
    public ActorsFilms getActorFilms(@RequestParam String actor) {
        try {
            log.info("查詢演員電影作品：{}", actor);

            String prompt = String.format(
                "Generate the filmography of 5 movies for %s. " +
                "Please provide the response in JSON format with 'actor' and 'movies' fields.",
                actor);

            ActorsFilms result = ChatClient.create(chatModel)
                    .prompt()
                    .user(prompt)
                    .call()
                    .entity(ActorsFilms.class);

            log.info("成功取得 {} 的電影作品，共 {} 部電影",
                    result.actor(), result.movies().size());

            return result;

        } catch (Exception e) {
            log.error("查詢演員電影作品失敗：{}", actor, e);
            return new ActorsFilms(actor, List.of("查詢失敗：" + e.getMessage()));
        }
    }

    /**
     * 多位演員的電影作品
     * @return 多位演員的電影作品列表
     */
    @GetMapping("/multiple-actors")
    public List<ActorsFilms> getMultipleActors() {
        try {
            log.info("查詢多位演員電影作品");

            String prompt = "Generate the filmography of 5 movies each for Tom Hanks and Bill Murray. " +
                          "Please provide the response as a JSON array with each object containing 'actor' and 'movies' fields.";

            List<ActorsFilms> results = ChatClient.create(chatModel)
                    .prompt()
                    .user(prompt)
                    .call()
                    .entity(new ParameterizedTypeReference<List<ActorsFilms>>() {});

            log.info("成功取得 {} 位演員的電影作品", results.size());

            return results;

        } catch (Exception e) {
            log.error("查詢多位演員電影作品失敗", e);
            return List.of(
                new ActorsFilms("Tom Hanks", List.of("查詢失敗：" + e.getMessage())),
                new ActorsFilms("Bill Murray", List.of("查詢失敗：" + e.getMessage()))
            );
        }
    }

    /**
     * 電影資訊查詢
     * @param movieTitle 電影標題
     * @return 電影詳細資訊
     */
    @GetMapping("/movie-info")
    public MovieInfo getMovieInfo(@RequestParam String movieTitle) {
        try {
            log.info("查詢電影資訊：{}", movieTitle);

            String prompt = String.format(
                "Provide detailed information about the movie '%s'. " +
                "Include title, director, year, genre, rating, and a brief plot summary. " +
                "Format the response as JSON.", movieTitle);

            MovieInfo result = ChatClient.create(chatModel)
                    .prompt()
                    .user(prompt)
                    .call()
                    .entity(MovieInfo.class);

            log.info("成功取得電影資訊：{} ({})", result.title(), result.year());

            return result;

        } catch (Exception e) {
            log.error("查詢電影資訊失敗：{}", movieTitle, e);
            return new MovieInfo(
                    movieTitle,
                    "未知",
                    0,
                    "未知",
                    0.0,
                    "查詢失敗：" + e.getMessage()
            );
        }
    }

    /**
     * 產品推薦列表
     * @param category 產品類別
     * @param count 推薦數量
     * @return 產品推薦列表
     */
    @GetMapping("/product-recommendations")
    public ProductRecommendations getProductRecommendations(
            @RequestParam String category,
            @RequestParam(defaultValue = "5") int count) {

        try {
            log.info("查詢產品推薦：類別={}，數量={}", category, count);

            String prompt = String.format(
                "Recommend %d popular %s products. " +
                "For each product, provide name, brand, price, rating, and description. " +
                "Format as JSON with 'category', 'count', and 'products' array.",
                count, category);

            ProductRecommendations result = ChatClient.create(chatModel)
                    .prompt()
                    .user(prompt)
                    .call()
                    .entity(ProductRecommendations.class);

            log.info("成功取得 {} 類別的 {} 個產品推薦",
                    result.category(), result.products().size());

            return result;

        } catch (Exception e) {
            log.error("查詢產品推薦失敗：類別={}", category, e);
            return new ProductRecommendations(
                    category,
                    0,
                    List.of(),
                    "查詢失敗：" + e.getMessage()
            );
        }
    }
}
