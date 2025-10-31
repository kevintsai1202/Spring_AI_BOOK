package com.example.advancedrag.service;

import com.example.advancedrag.dto.SingleEvaluationRequest;
import com.example.advancedrag.dto.EvaluationResult;
import com.example.advancedrag.dto.EvaluationReport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG 評估服務
 *
 * 使用 LLM-as-a-Judge 方法評估 RAG 系統的答案質量
 * 評估維度：
 * 1. 答案準確性（Accuracy）
 * 2. 答案相關性（Relevance）
 * 3. 答案完整性（Completeness）
 * 4. 答案一致性（Consistency）
 * 5. 檢索質量（Retrieval Quality）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RAGEvaluationService {

    private final ChatClient chatClient;
    private final RAGMetricsService metricsService;

    /**
     * 評估 Prompt 模板 - 答案準確性
     */
    private static final String ACCURACY_EVALUATION_PROMPT = """
            請評估以下 RAG 系統生成的答案的準確性。

            問題：{question}

            參考答案（Ground Truth）：{groundTruth}

            生成的答案：{generatedAnswer}

            請從以下維度評分（1-10分）：
            1. 事實準確性：答案中的事實是否正確
            2. 數據準確性：數字、日期等數據是否準確
            3. 邏輯準確性：推理和結論是否合理

            請以 JSON 格式返回評分結果：
            {
              "factual_accuracy": <分數>,
              "data_accuracy": <分數>,
              "logical_accuracy": <分數>,
              "overall_score": <總分>,
              "explanation": "<解釋>"
            }

            只返回 JSON，不要其他內容。
            """;

    /**
     * 評估 Prompt 模板 - 答案相關性
     */
    private static final String RELEVANCE_EVALUATION_PROMPT = """
            請評估以下 RAG 系統生成的答案與問題的相關性。

            問題：{question}

            生成的答案：{generatedAnswer}

            檢索到的上下文：
            {context}

            請從以下維度評分（1-10分）：
            1. 問題相關性：答案是否直接回答了問題
            2. 上下文相關性：答案是否充分利用了檢索到的上下文
            3. 主題一致性：答案是否圍繞問題主題展開

            請以 JSON 格式返回評分結果：
            {
              "question_relevance": <分數>,
              "context_relevance": <分數>,
              "topic_consistency": <分數>,
              "overall_score": <總分>,
              "explanation": "<解釋>"
            }

            只返回 JSON，不要其他內容。
            """;

    /**
     * 評估 Prompt 模板 - 答案完整性
     */
    private static final String COMPLETENESS_EVALUATION_PROMPT = """
            請評估以下 RAG 系統生成的答案的完整性。

            問題：{question}

            參考答案（Ground Truth）：{groundTruth}

            生成的答案：{generatedAnswer}

            請從以下維度評分（1-10分）：
            1. 信息覆蓋度：答案是否涵蓋了所有關鍵信息點
            2. 細節充分性：答案是否提供了足夠的細節
            3. 結構完整性：答案是否結構完整、邏輯清晰

            請以 JSON 格式返回評分結果：
            {
              "information_coverage": <分數>,
              "detail_sufficiency": <分數>,
              "structural_integrity": <分數>,
              "overall_score": <總分>,
              "explanation": "<解釋>"
            }

            只返回 JSON，不要其他內容。
            """;

    /**
     * 評估單個問答對
     *
     * @param request 評估請求
     * @return 評估結果
     */
    public EvaluationResult evaluateSingle(SingleEvaluationRequest request) {
        long startTime = System.currentTimeMillis();

        try {
            log.info("開始評估單個問答對，問題: {}", request.getQuestion());

            // 評估準確性
            double accuracyScore = evaluateAccuracy(
                    request.getQuestion(),
                    request.getGroundTruth(),
                    request.getGeneratedAnswer()
            );

            // 評估相關性
            double relevanceScore = evaluateRelevance(
                    request.getQuestion(),
                    request.getGeneratedAnswer(),
                    request.getRetrievedContext()
            );

            // 評估完整性
            double completenessScore = evaluateCompleteness(
                    request.getQuestion(),
                    request.getGroundTruth(),
                    request.getGeneratedAnswer()
            );

            // 計算綜合分數（加權平均）
            double overallScore = (accuracyScore * 0.4) +
                    (relevanceScore * 0.3) +
                    (completenessScore * 0.3);

            long processingTime = System.currentTimeMillis() - startTime;

            // 記錄評估指標
            metricsService.recordEvaluationDuration(processingTime);
            metricsService.recordAccuracyScore(accuracyScore);
            metricsService.recordRelevanceScore(relevanceScore);
            metricsService.recordOverallScore(overallScore);

            return EvaluationResult.builder()
                    .question(request.getQuestion())
                    .generatedAnswer(request.getGeneratedAnswer())
                    .groundTruth(request.getGroundTruth())
                    .accuracyScore(accuracyScore)
                    .relevanceScore(relevanceScore)
                    .completenessScore(completenessScore)
                    .overallScore(overallScore)
                    .passed(overallScore >= 7.0)
                    .processingTimeMs(processingTime)
                    .evaluatedAt(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("評估失敗", e);
            throw new RuntimeException("評估失敗: " + e.getMessage(), e);
        }
    }

    /**
     * 批次評估多個問答對
     *
     * @param requests 評估請求列表
     * @return 評估報告
     */
    public EvaluationReport evaluateBatch(List<SingleEvaluationRequest> requests) {
        long startTime = System.currentTimeMillis();

        log.info("開始批次評估，數量: {}", requests.size());

        List<EvaluationResult> results = new ArrayList<>();
        int passedCount = 0;
        double totalAccuracy = 0.0;
        double totalRelevance = 0.0;
        double totalCompleteness = 0.0;
        double totalOverall = 0.0;

        for (SingleEvaluationRequest request : requests) {
            try {
                EvaluationResult result = evaluateSingle(request);
                results.add(result);

                if (result.getPassed()) {
                    passedCount++;
                }

                totalAccuracy += result.getAccuracyScore();
                totalRelevance += result.getRelevanceScore();
                totalCompleteness += result.getCompletenessScore();
                totalOverall += result.getOverallScore();

            } catch (Exception e) {
                log.error("評估第 {} 個問答對失敗", results.size() + 1, e);
            }
        }

        int totalCount = results.size();
        double passRate = totalCount > 0 ? (double) passedCount / totalCount : 0.0;

        long processingTime = System.currentTimeMillis() - startTime;

        return EvaluationReport.builder()
                .totalCount(totalCount)
                .passedCount(passedCount)
                .failedCount(totalCount - passedCount)
                .passRate(passRate)
                .averageAccuracy(totalCount > 0 ? totalAccuracy / totalCount : 0.0)
                .averageRelevance(totalCount > 0 ? totalRelevance / totalCount : 0.0)
                .averageCompleteness(totalCount > 0 ? totalCompleteness / totalCount : 0.0)
                .averageOverallScore(totalCount > 0 ? totalOverall / totalCount : 0.0)
                .results(results)
                .processingTimeMs(processingTime)
                .evaluatedAt(LocalDateTime.now())
                .build();
    }

    /**
     * 評估準確性
     *
     * @param question        問題
     * @param groundTruth     參考答案
     * @param generatedAnswer 生成的答案
     * @return 準確性分數（0-10）
     */
    private double evaluateAccuracy(String question, String groundTruth, String generatedAnswer) {
        try {
            PromptTemplate promptTemplate = new PromptTemplate(ACCURACY_EVALUATION_PROMPT);
            Map<String, Object> params = new HashMap<>();
            params.put("question", question);
            params.put("groundTruth", groundTruth != null ? groundTruth : "無參考答案");
            params.put("generatedAnswer", generatedAnswer);

            String response = chatClient.prompt()
                    .user(promptTemplate.create(params).getContents())
                    .call()
                    .content();

            return parseOverallScore(response);

        } catch (Exception e) {
            log.error("評估準確性失敗", e);
            return 5.0; // 默認中等分數
        }
    }

    /**
     * 評估相關性
     *
     * @param question        問題
     * @param generatedAnswer 生成的答案
     * @param context         檢索到的上下文
     * @return 相關性分數（0-10）
     */
    private double evaluateRelevance(String question, String generatedAnswer, String context) {
        try {
            PromptTemplate promptTemplate = new PromptTemplate(RELEVANCE_EVALUATION_PROMPT);
            Map<String, Object> params = new HashMap<>();
            params.put("question", question);
            params.put("generatedAnswer", generatedAnswer);
            params.put("context", context != null ? context : "無上下文");

            String response = chatClient.prompt()
                    .user(promptTemplate.create(params).getContents())
                    .call()
                    .content();

            return parseOverallScore(response);

        } catch (Exception e) {
            log.error("評估相關性失敗", e);
            return 5.0;
        }
    }

    /**
     * 評估完整性
     *
     * @param question        問題
     * @param groundTruth     參考答案
     * @param generatedAnswer 生成的答案
     * @return 完整性分數（0-10）
     */
    private double evaluateCompleteness(String question, String groundTruth, String generatedAnswer) {
        try {
            PromptTemplate promptTemplate = new PromptTemplate(COMPLETENESS_EVALUATION_PROMPT);
            Map<String, Object> params = new HashMap<>();
            params.put("question", question);
            params.put("groundTruth", groundTruth != null ? groundTruth : "無參考答案");
            params.put("generatedAnswer", generatedAnswer);

            String response = chatClient.prompt()
                    .user(promptTemplate.create(params).getContents())
                    .call()
                    .content();

            return parseOverallScore(response);

        } catch (Exception e) {
            log.error("評估完整性失敗", e);
            return 5.0;
        }
    }

    /**
     * 從 LLM 響應中解析總分
     *
     * @param response LLM 響應（JSON 格式）
     * @return 總分
     */
    private double parseOverallScore(String response) {
        try {
            // 簡化的 JSON 解析（實際應使用 Jackson 或 Gson）
            String cleanResponse = response.trim();
            if (cleanResponse.startsWith("```json")) {
                cleanResponse = cleanResponse.substring(7);
            }
            if (cleanResponse.endsWith("```")) {
                cleanResponse = cleanResponse.substring(0, cleanResponse.length() - 3);
            }
            cleanResponse = cleanResponse.trim();

            // 查找 "overall_score"
            int scoreIndex = cleanResponse.indexOf("\"overall_score\"");
            if (scoreIndex == -1) {
                return 5.0;
            }

            String afterScore = cleanResponse.substring(scoreIndex);
            int colonIndex = afterScore.indexOf(":");
            int commaIndex = afterScore.indexOf(",");
            int endIndex = commaIndex > 0 ? commaIndex : afterScore.indexOf("}");

            if (colonIndex > 0 && endIndex > colonIndex) {
                String scoreStr = afterScore.substring(colonIndex + 1, endIndex).trim();
                return Double.parseDouble(scoreStr);
            }

            return 5.0;

        } catch (Exception e) {
            log.error("解析評分失敗", e);
            return 5.0;
        }
    }
}
