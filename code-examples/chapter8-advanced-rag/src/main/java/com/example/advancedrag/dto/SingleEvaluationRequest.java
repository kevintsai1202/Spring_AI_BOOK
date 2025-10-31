package com.example.advancedrag.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 單個問答對評估請求
 *
 * 用於評估單個 RAG 生成的答案
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SingleEvaluationRequest {

    /**
     * 問題
     */
    @NotBlank(message = "問題不能為空")
    private String question;

    /**
     * RAG 生成的答案
     */
    @NotBlank(message = "生成的答案不能為空")
    private String generatedAnswer;

    /**
     * 參考答案（Ground Truth）- 可選
     */
    private String groundTruth;

    /**
     * 檢索到的上下文 - 可選
     */
    private String retrievedContext;

    /**
     * 是否評估準確性
     */
    @Builder.Default
    private Boolean evaluateAccuracy = true;

    /**
     * 是否評估相關性
     */
    @Builder.Default
    private Boolean evaluateRelevance = true;

    /**
     * 是否評估完整性
     */
    @Builder.Default
    private Boolean evaluateCompleteness = true;
}
