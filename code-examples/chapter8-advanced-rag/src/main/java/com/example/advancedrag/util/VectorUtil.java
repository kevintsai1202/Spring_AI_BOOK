package com.example.advancedrag.util;

import java.util.List;

/**
 * 向量計算工具類
 *
 * 提供向量相關的數學計算功能
 */
public class VectorUtil {

    /**
     * 計算兩個向量的餘弦相似度
     *
     * @param vec1 向量1
     * @param vec2 向量2
     * @return 餘弦相似度（-1 到 1 之間）
     */
    public static double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("向量不能為空且維度必須相同");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 計算兩個向量的餘弦相似度（float[] 版本）
     *
     * @param vec1 向量1
     * @param vec2 向量2
     * @return 餘弦相似度（-1 到 1 之間）
     */
    public static double cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1 == null || vec2 == null || vec1.length != vec2.length) {
            throw new IllegalArgumentException("向量不能為空且維度必須相同");
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 計算向量的歐幾里得距離
     *
     * @param vec1 向量1
     * @param vec2 向量2
     * @return 歐幾里得距離
     */
    public static double euclideanDistance(List<Double> vec1, List<Double> vec2) {
        if (vec1 == null || vec2 == null || vec1.size() != vec2.size()) {
            throw new IllegalArgumentException("向量不能為空且維度必須相同");
        }

        double sum = 0.0;
        for (int i = 0; i < vec1.size(); i++) {
            double diff = vec1.get(i) - vec2.get(i);
            sum += diff * diff;
        }

        return Math.sqrt(sum);
    }

    /**
     * 正規化向量（L2 正規化）
     *
     * @param vector 原始向量
     * @return 正規化後的向量
     */
    public static List<Double> normalize(List<Double> vector) {
        if (vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("向量不能為空");
        }

        double norm = 0.0;
        for (Double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);

        if (norm == 0.0) {
            return vector;
        }

        // 創建 final 副本供 lambda 使用
        final double finalNorm = norm;
        return vector.stream()
                .map(v -> v / finalNorm)
                .toList();
    }
}
