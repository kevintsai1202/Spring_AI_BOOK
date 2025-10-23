/**
 * 文本向量化服務
 * 負責將文本轉換為向量嵌入
 */
package com.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TextEmbeddingService {
    
    private final EmbeddingModel embeddingModel;
    private final MeterRegistry meterRegistry;
    
    /**
     * 單一文本向量化
     */
    public float[] embedText(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Text cannot be null or empty");
        }
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // 預處理文本
            String processedText = preprocessText(text);
            
            // 生成向量
            float[] embedding = embeddingModel.embed(processedText);
            
            // 記錄指標
            sample.stop(Timer.builder("embedding.generation.time")
                .description("Time to generate text embedding")
                .register(meterRegistry));
            
            meterRegistry.counter("embedding.generation.count").increment();
            
            log.debug("Generated embedding for text: {} chars, vector dim: {}", 
                text.length(), embedding.length);
            
            return embedding;
            
        } catch (Exception e) {
            meterRegistry.counter("embedding.generation.errors").increment();
            log.error("Failed to generate embedding for text: {}", text, e);
            throw new EmbeddingException("Failed to generate embedding", e);
        }
    }
    
    /**
     * 批次文本向量化
     */
    public List<float[]> embedTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        
        log.info("Generating embeddings for {} texts", texts.size());
        
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // 預處理所有文本
            List<String> processedTexts = texts.stream()
                .map(this::preprocessText)
                .collect(Collectors.toList());
            
            // 批次生成向量
            List<float[]> embeddings = embeddingModel.embed(processedTexts);
            
            sample.stop(Timer.builder("embedding.batch.generation.time")
                .description("Time to generate batch embeddings")
                .register(meterRegistry));
            
            meterRegistry.counter("embedding.batch.generation.count")
                .increment(texts.size());
            
            log.info("Successfully generated {} embeddings", embeddings.size());
            
            return embeddings;
            
        } catch (Exception e) {
            meterRegistry.counter("embedding.batch.generation.errors").increment();
            log.error("Failed to generate batch embeddings", e);
            throw new EmbeddingException("Failed to generate batch embeddings", e);
        }
    }
    
    /**
     * 文本預處理
     */
    private String preprocessText(String text) {
        if (text == null) {
            return "";
        }
        
        return text
            .trim()
            .replaceAll("\\s+", " ")  // 標準化空白字符
            .replaceAll("[\\r\\n]+", " ")  // 移除換行符
            .substring(0, Math.min(text.length(), 8000));  // 限制長度
    }
    
    /**
     * 計算向量相似性
     */
    public double calculateSimilarity(List<Double> vector1, List<Double> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException("Vectors must have the same dimension");
        }
        
        // 餘弦相似性計算
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vector1.size(); i++) {
            dotProduct += vector1.get(i) * vector2.get(i);
            norm1 += Math.pow(vector1.get(i), 2);
            norm2 += Math.pow(vector2.get(i), 2);
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 自定義異常類
     */
    public static class EmbeddingException extends RuntimeException {
        public EmbeddingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}