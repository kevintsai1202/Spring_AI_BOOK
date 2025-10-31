package com.example.memory.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 內容過濾 Advisor
 *
 * 負責過濾敏感詞和不適當的內容
 * 可保護系統安全和用戶隱私
 */
@Slf4j
@Component
public class ContentFilterAdvisor implements Advisor {

    /**
     * 敏感詞列表（示例）
     */
    private static final Set<String> SENSITIVE_WORDS = new HashSet<>(Arrays.asList(
        "sensitive_word_1",
        "sensitive_word_2",
        "inappropriate_content",
        "vulgar_language"
    ));

    /**
     * 返回Advisor名稱
     */
    @Override
    public String getName() {
        return "ContentFilterAdvisor";
    }

    /**
     * 執行順序：100（靠前執行，安全檢查）
     */
    @Override
    public int getOrder() {
        return 100;
    }

    /**
     * 請求前：過濾用戶輸入
     */
    @Override
    public AdvisorContext adviseRequest(AdvisorContext context) {
        log.debug("Filtering content in user message");

        String filteredMessage = filterContent(context.getUserMessage());

        if (!filteredMessage.equals(context.getUserMessage())) {
            log.info("Content filtered - Original length: {}, Filtered length: {}",
                context.getUserMessage().length(), filteredMessage.length());
        }

        context.setModifiedPrompt(filteredMessage);
        context.putMetadata("content_filtered", !filteredMessage.equals(context.getUserMessage()));

        return context;
    }

    /**
     * 回應後：過濾AI回應
     */
    @Override
    public AdvisorContext adviseResponse(AdvisorContext context) {
        if (context.isError()) {
            return context;
        }

        log.debug("Filtering content in AI response");

        String filteredResponse = filterContent(context.getAiResponse());

        if (!filteredResponse.equals(context.getAiResponse())) {
            log.info("Response content filtered");
        }

        context.setAiResponse(filteredResponse);

        return context;
    }

    /**
     * 執行內容過濾
     *
     * @param content 原始內容
     * @return 過濾後的內容
     */
    private String filterContent(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        String filtered = content;

        // 替換敏感詞為***
        for (String sensitiveWord : SENSITIVE_WORDS) {
            filtered = filtered.replaceAll("(?i)" + sensitiveWord, "***");
        }

        return filtered;
    }

    /**
     * 檢查內容是否包含敏感詞
     *
     * @param content 內容
     * @return 是否包含敏感詞
     */
    public boolean containsSensitiveWords(String content) {
        if (content == null) {
            return false;
        }

        String lowerContent = content.toLowerCase();
        return SENSITIVE_WORDS.stream()
            .anyMatch(word -> lowerContent.contains(word.toLowerCase()));
    }

    /**
     * 添加敏感詞到過濾列表
     */
    public void addSensitiveWord(String word) {
        SENSITIVE_WORDS.add(word);
        log.info("Added sensitive word: {}", word);
    }

    /**
     * 移除敏感詞
     */
    public void removeSensitiveWord(String word) {
        SENSITIVE_WORDS.remove(word);
        log.info("Removed sensitive word: {}", word);
    }
}
