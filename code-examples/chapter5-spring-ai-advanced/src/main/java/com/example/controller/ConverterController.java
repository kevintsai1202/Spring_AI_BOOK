package com.example.controller;

import com.example.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 轉換器控制器
 * 使用傳統的 BeanOutputConverter、MapOutputConverter、ListOutputConverter
 * 對應 5.10 章節的傳統轉換器實現
 */
@RestController
@RequestMapping("/api/converter")
@RequiredArgsConstructor
@Slf4j
public class ConverterController {

    private final ChatModel chatModel;

    /**
     * Bean Output Converter 範例
     * @param actor 演員名稱
     * @return 演員電影作品
     */
    @GetMapping("/actor-films-converter")
    public ActorsFilms getActorFilmsWithConverter(@RequestParam String actor) {
        try {
            log.info("使用 BeanOutputConverter 查詢演員電影作品：{}", actor);

            BeanOutputConverter<ActorsFilms> beanOutputConverter =
                    new BeanOutputConverter<>(ActorsFilms.class);
            String format = beanOutputConverter.getFormat();

            log.debug("轉換器格式：{}", format);

            String prompt = String.format(
                "Generate the filmography of 5 movies for %s. %s",
                actor, format);

            String response = ChatClient.create(chatModel)
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("AI 回應：{}", response);

            ActorsFilms result = beanOutputConverter.convert(response);

            log.info("成功轉換演員電影作品：{}", result.actor());

            return result;

        } catch (Exception e) {
            log.error("BeanOutputConverter 查詢失敗：{}", actor, e);
            return new ActorsFilms(actor, List.of("查詢失敗：" + e.getMessage()));
        }
    }

    /**
     * Map Output Converter 範例
     * @return 數字資料的 Map 格式
     */
    @GetMapping("/numbers-map")
    public Map<String, Object> getNumbersAsMap() {
        try {
            log.info("使用 MapOutputConverter 查詢數字資料");

            MapOutputConverter mapOutputConverter = new MapOutputConverter();
            String format = mapOutputConverter.getFormat();

            log.debug("轉換器格式：{}", format);

            String prompt = String.format(
                "Provide interesting statistics about the number 42. %s", format);

            String response = ChatClient.create(chatModel)
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("AI 回應：{}", response);

            Map<String, Object> result = mapOutputConverter.convert(response);

            log.info("成功轉換為 Map，包含 {} 個鍵值對", result.size());

            return result;

        } catch (Exception e) {
            log.error("MapOutputConverter 查詢失敗", e);
            return Map.of("error", "查詢失敗：" + e.getMessage());
        }
    }

    /**
     * List Output Converter 範例
     * @param category 類別
     * @param count 數量
     * @return 項目列表
     */
    @GetMapping("/items-list")
    public List<String> getItemsList(
            @RequestParam String category,
            @RequestParam(defaultValue = "5") int count) {

        try {
            log.info("使用 ListOutputConverter 查詢項目列表：類別={}，數量={}", category, count);

            ListOutputConverter listOutputConverter =
                    new ListOutputConverter(new DefaultConversionService());
            String format = listOutputConverter.getFormat();

            log.debug("轉換器格式：{}", format);

            String prompt = String.format(
                "List %d popular %s items. %s",
                count, category, format);

            String response = ChatClient.create(chatModel)
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("AI 回應：{}", response);

            List<String> result = listOutputConverter.convert(response);

            log.info("成功轉換為列表，包含 {} 個項目", result.size());

            return result;

        } catch (Exception e) {
            log.error("ListOutputConverter 查詢失敗：類別={}", category, e);
            return List.of("查詢失敗：" + e.getMessage());
        }
    }

    /**
     * 複雜業務分析（使用 BeanOutputConverter）
     * @param businessData 業務資料
     * @return 分析結果
     */
    @PostMapping("/business-analysis")
    public BusinessAnalysis analyzeBusinessData(@RequestBody String businessData) {
        try {
            log.info("使用 BeanOutputConverter 進行業務分析");

            BeanOutputConverter<BusinessAnalysis> converter =
                    new BeanOutputConverter<>(BusinessAnalysis.class);
            String format = converter.getFormat();

            String prompt = String.format(
                "Analyze the following business data and provide insights: \n%s\n\n" +
                "Please provide a comprehensive analysis including summary, key metrics, " +
                "recommendations, risk factors, and confidence score. %s",
                businessData, format);

            String response = ChatClient.create(chatModel)
                    .prompt()
                    .user(prompt)
                    .call()
                    .content();

            BusinessAnalysis result = converter.convert(response);

            log.info("業務分析完成，信心分數：{}", result.confidenceScore());

            return result;

        } catch (Exception e) {
            log.error("業務分析失敗", e);
            return new BusinessAnalysis(
                    "分析失敗：" + e.getMessage(),
                    List.of(),
                    List.of(),
                    List.of("系統錯誤"),
                    0.0
            );
        }
    }
}
