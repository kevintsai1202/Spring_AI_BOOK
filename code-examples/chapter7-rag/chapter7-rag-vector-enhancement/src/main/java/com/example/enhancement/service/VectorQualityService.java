package com.example.enhancement.service;

import com.example.enhancement.config.VectorQualityConfig;
import com.example.enhancement.model.EmbeddingQuality;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 向量品質評估服務
 *
 * 提供向量品質評估功能：
 * 1. 維度驗證
 * 2. 範數檢查（L2 norm）
 * 3. 數值範圍檢查
 * 4. 零向量檢查
 * 5. 文本長度驗證
 * 6. 品質評分計算
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VectorQualityService {

    private final EmbeddingModel embeddingModel;
    private final VectorQualityConfig config;

    /**
     * 評估文檔列表的向量品質
     *
     * @param documents 文檔列表
     * @return 品質評估結果列表
     */
    public List<EmbeddingQuality> assessDocumentsQuality(List<Document> documents) {
        log.info("Assessing vector quality for {} documents", documents.size());

        List<EmbeddingQuality> qualityResults = new ArrayList<>();

        for (Document document : documents) {
            try {
                EmbeddingQuality quality = assessDocumentQuality(document);
                qualityResults.add(quality);

                if (!quality.isValid()) {
                    log.warn("Document quality assessment failed: {}", quality.getValidationMessage());
                }
            } catch (Exception e) {
                log.error("Error assessing document quality", e);
                qualityResults.add(createInvalidQuality("Assessment error: " + e.getMessage()));
            }
        }

        log.info("Quality assessment completed. Valid: {}, Invalid: {}",
                qualityResults.stream().filter(EmbeddingQuality::isValid).count(),
                qualityResults.stream().filter(q -> !q.isValid()).count());

        return qualityResults;
    }

    /**
     * 評估單個文檔的向量品質
     *
     * @param document 文檔
     * @return 品質評估結果
     */
    public EmbeddingQuality assessDocumentQuality(Document document) {
        String content = document.getFormattedContent();

        // 1. 驗證文本長度
        if (!isValidTextLength(content)) {
            return createInvalidQuality("Invalid text length: " + content.length());
        }

        // 2. 生成向量
        try {
            EmbeddingResponse response = embeddingModel.embedForResponse(List.of(content));
            float[] embedding = response.getResults().get(0).getOutput();

            // 3. 評估向量品質
            return assessEmbeddingQuality(embedding, content.length());

        } catch (Exception e) {
            log.error("Error generating embedding for quality assessment", e);
            return createInvalidQuality("Embedding generation failed: " + e.getMessage());
        }
    }

    /**
     * 評估向量品質
     *
     * @param embedding  向量
     * @param textLength 文本長度
     * @return 品質評估結果
     */
    public EmbeddingQuality assessEmbeddingQuality(float[] embedding, int textLength) {
        // 1. 驗證維度
        int dimension = embedding.length;
        boolean validDimension = isValidDimension(dimension);

        // 2. 檢查是否為零向量
        boolean notZeroVector = !isZeroVector(embedding);

        // 3. 計算統計資訊
        double norm = calculateNorm(embedding);
        double mean = calculateMean(embedding);
        double stdDev = calculateStandardDeviation(embedding, mean);
        double minValue = findMin(embedding);
        double maxValue = findMax(embedding);

        // 4. 驗證範數
        boolean validNorm = isValidNorm(norm);

        // 5. 驗證數值範圍
        boolean validRange = isValidRange(minValue, maxValue);

        // 6. 驗證文本長度
        boolean validTextLength = isValidTextLength(textLength);

        // 7. 計算總體品質分數
        double qualityScore = calculateQualityScore(
                validDimension, validNorm, validRange, notZeroVector, validTextLength, stdDev);

        // 8. 判斷是否為有效向量
        boolean isValid = validDimension && validNorm && validRange && notZeroVector && validTextLength;

        // 9. 生成驗證訊息
        String validationMessage = generateValidationMessage(
                validDimension, validNorm, validRange, notZeroVector, validTextLength);

        return EmbeddingQuality.builder()
                .dimension(dimension)
                .norm(norm)
                .mean(mean)
                .standardDeviation(stdDev)
                .qualityScore(qualityScore)
                .isValid(isValid)
                .validDimension(validDimension)
                .validRange(validRange)
                .validNorm(validNorm)
                .notZeroVector(notZeroVector)
                .validTextLength(validTextLength)
                .minValue(minValue)
                .maxValue(maxValue)
                .validationMessage(validationMessage)
                .build();
    }

    /**
     * 驗證維度是否有效
     */
    private boolean isValidDimension(int dimension) {
        return dimension >= config.getMinDimension() && dimension <= config.getMaxDimension();
    }

    /**
     * 驗證範數是否有效
     */
    private boolean isValidNorm(double norm) {
        return norm >= config.getMinNorm() && norm <= config.getMaxNorm();
    }

    /**
     * 驗證數值範圍是否有效
     */
    private boolean isValidRange(double minValue, double maxValue) {
        // 檢查是否有無效值（NaN, Infinity）
        return !Double.isNaN(minValue) && !Double.isNaN(maxValue) &&
                !Double.isInfinite(minValue) && !Double.isInfinite(maxValue);
    }

    /**
     * 檢查是否為零向量
     */
    private boolean isZeroVector(float[] embedding) {
        for (float value : embedding) {
            if (Math.abs(value) > 1e-9) {
                return false;
            }
        }
        return true;
    }

    /**
     * 驗證文本長度
     */
    private boolean isValidTextLength(String text) {
        return isValidTextLength(text.length());
    }

    /**
     * 驗證文本長度
     */
    private boolean isValidTextLength(int length) {
        return length >= config.getMinTextLength() && length <= config.getMaxTextLength();
    }

    /**
     * 計算向量範數（L2 norm）
     */
    private double calculateNorm(float[] embedding) {
        double sumSquares = 0.0;
        for (float value : embedding) {
            sumSquares += value * value;
        }
        return Math.sqrt(sumSquares);
    }

    /**
     * 計算向量平均值
     */
    private double calculateMean(float[] embedding) {
        double sum = 0.0;
        for (float value : embedding) {
            sum += value;
        }
        return sum / embedding.length;
    }

    /**
     * 計算向量標準差
     */
    private double calculateStandardDeviation(float[] embedding, double mean) {
        double sumSquaredDiff = 0.0;
        for (float value : embedding) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        return Math.sqrt(sumSquaredDiff / embedding.length);
    }

    /**
     * 找出向量最小值
     */
    private double findMin(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            return Double.NaN;
        }
        float min = embedding[0];
        for (int i = 1; i < embedding.length; i++) {
            if (embedding[i] < min) {
                min = embedding[i];
            }
        }
        return min;
    }

    /**
     * 找出向量最大值
     */
    private double findMax(float[] embedding) {
        if (embedding == null || embedding.length == 0) {
            return Double.NaN;
        }
        float max = embedding[0];
        for (int i = 1; i < embedding.length; i++) {
            if (embedding[i] > max) {
                max = embedding[i];
            }
        }
        return max;
    }

    /**
     * 計算品質分數（0.0 - 1.0）
     */
    private double calculateQualityScore(boolean validDimension, boolean validNorm,
                                          boolean validRange, boolean notZeroVector,
                                          boolean validTextLength, double stdDev) {
        double score = 0.0;

        // 各項檢查的權重
        if (validDimension) score += 0.2;
        if (validNorm) score += 0.2;
        if (validRange) score += 0.2;
        if (notZeroVector) score += 0.2;
        if (validTextLength) score += 0.1;

        // 標準差加分項（標準差越大，向量資訊量越豐富）
        // 標準差 > 0.1 加 0.1 分
        if (stdDev > 0.1) score += 0.1;

        return Math.min(1.0, score);
    }

    /**
     * 生成驗證訊息
     */
    private String generateValidationMessage(boolean validDimension, boolean validNorm,
                                              boolean validRange, boolean notZeroVector,
                                              boolean validTextLength) {
        List<String> issues = new ArrayList<>();

        if (!validDimension) issues.add("Invalid dimension");
        if (!validNorm) issues.add("Invalid norm");
        if (!validRange) issues.add("Invalid value range");
        if (!notZeroVector) issues.add("Zero vector detected");
        if (!validTextLength) issues.add("Invalid text length");

        return issues.isEmpty() ? "Valid" : String.join(", ", issues);
    }

    /**
     * 創建無效品質結果
     */
    private EmbeddingQuality createInvalidQuality(String message) {
        return EmbeddingQuality.builder()
                .isValid(false)
                .validationMessage(message)
                .qualityScore(0.0)
                .build();
    }

    /**
     * 批次評估向量品質並過濾低品質向量
     *
     * @param documents 文檔列表
     * @return 高品質文檔列表
     */
    public List<Document> filterHighQualityDocuments(List<Document> documents) {
        log.info("Filtering high quality documents from {} documents", documents.size());

        List<Document> highQualityDocs = new ArrayList<>();

        for (Document document : documents) {
            EmbeddingQuality quality = assessDocumentQuality(document);

            if (quality.isValid() && quality.getQualityScore() >= config.getQualityThreshold()) {
                highQualityDocs.add(document);
            } else {
                log.debug("Document filtered due to low quality. Score: {}, Message: {}",
                        quality.getQualityScore(), quality.getValidationMessage());
            }
        }

        log.info("Filtered {} high quality documents from {} total documents",
                highQualityDocs.size(), documents.size());

        return highQualityDocs;
    }
}
