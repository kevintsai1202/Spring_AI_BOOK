package com.example.advancedrag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * BM25 評分服務
 *
 * 實現 BM25 算法用於計算文檔與查詢的相關性分數
 * BM25 是一種基於詞頻的排序算法，廣泛用於信息檢索
 */
@Slf4j
@Service
public class BM25Service {

    // BM25 參數
    private static final double K1 = 1.2;  // 詞頻飽和參數
    private static final double B = 0.75;  // 長度正規化參數

    /**
     * 計算 BM25 分數
     *
     * @param content 文檔內容
     * @param query 查詢文本
     * @param keywords 關鍵詞列表
     * @return BM25 分數（0-1）
     */
    public double calculateBM25Score(String content, String query, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return 0.5; // 無關鍵詞時返回中等分數
        }

        String lowerContent = content.toLowerCase();
        String lowerQuery = query.toLowerCase();

        // 簡化版 BM25：基於關鍵詞匹配和詞頻
        double score = 0.0;
        int totalMatches = 0;

        for (String keyword : keywords) {
            String lowerKeyword = keyword.toLowerCase();
            int termFreq = countOccurrences(lowerContent, lowerKeyword);

            if (termFreq > 0) {
                totalMatches++;

                // TF 部分（詞頻）使用 BM25 飽和函數
                // BM25 TF: (tf * (k1 + 1)) / (tf + k1 * (1 - b + b * (docLength / avgDocLength)))
                // 簡化版本：不計算平均文檔長度
                double tf = (double) termFreq * (K1 + 1) / (termFreq + K1);
                score += tf;
            }
        }

        // 正規化分數
        if (!keywords.isEmpty()) {
            score = score / keywords.size();
        }

        // 額外加分：查詢完整匹配
        if (lowerContent.contains(lowerQuery)) {
            score = Math.min(1.0, score + 0.2);
        }

        return Math.min(1.0, score);
    }

    /**
     * 計算關鍵詞在內容中出現的次數
     *
     * @param content 內容
     * @param keyword 關鍵詞
     * @return 出現次數
     */
    private int countOccurrences(String content, String keyword) {
        if (content == null || keyword == null || keyword.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;

        while ((index = content.indexOf(keyword, index)) != -1) {
            count++;
            index += keyword.length();
        }

        return count;
    }
}
