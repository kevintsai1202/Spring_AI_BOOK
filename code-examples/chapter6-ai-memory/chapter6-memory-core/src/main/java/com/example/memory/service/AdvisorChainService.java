package com.example.memory.service;

import com.example.memory.advisor.Advisor;
import com.example.memory.advisor.AdvisorContext;
import com.example.memory.advisor.ContentFilterAdvisor;
import com.example.memory.advisor.LoggingAdvisor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advisor鏈服務
 *
 * 管理所有的Advisor並按優先級執行
 * 實現責任鏈模式
 */
@Slf4j
@Service
public class AdvisorChainService {

    /**
     * Spring應用上下文，用於動態獲取Advisor實現
     */
    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Advisor列表緩存
     */
    private List<Advisor> advisors;

    /**
     * 執行請求前的Advisor鏈
     *
     * @param context Advisor上下文
     * @return 修改後的Advisor上下文
     */
    public AdvisorContext executeChain(AdvisorContext context) {
        log.info("Executing advisor chain - Conversation: {}", context.getConversationId());

        List<Advisor> chain = getAdvisors();

        // 按優先級排序（從小到大）
        chain.sort(Comparator.comparingInt(Advisor::getOrder));

        // 執行所有Advisor的前置處理
        for (Advisor advisor : chain) {
            if (!advisor.supports(context)) {
                log.debug("Advisor {} does not support this context", advisor.getName());
                continue;
            }

            log.debug("Executing advisor: {} (order: {})", advisor.getName(), advisor.getOrder());

            try {
                context = advisor.adviseRequest(context);

                if (context.isError()) {
                    log.error("Advisor {} returned error: {}", advisor.getName(), context.getErrorMessage());
                    break;
                }
            } catch (Exception e) {
                log.error("Error in advisor {}", advisor.getName(), e);
                context.setError(true);
                context.setErrorMessage("Advisor執行錯誤: " + e.getMessage());
                break;
            }
        }

        return context;
    }

    /**
     * 執行回應後的Advisor鏈
     *
     * @param context Advisor上下文
     * @return 修改後的Advisor上下文
     */
    public AdvisorContext executeAfterChain(AdvisorContext context) {
        log.info("Executing after chain - Conversation: {}", context.getConversationId());

        List<Advisor> chain = getAdvisors();

        // 按優先級反序排列（從大到小），這樣回應會按相反順序處理
        chain.sort(Comparator.comparingInt(Advisor::getOrder).reversed());

        // 執行所有Advisor的後置處理
        for (Advisor advisor : chain) {
            if (!advisor.supports(context)) {
                continue;
            }

            log.debug("Executing after advisor: {} (order: {})", advisor.getName(), advisor.getOrder());

            try {
                context = advisor.adviseResponse(context);

                if (context.isError()) {
                    log.error("Advisor {} returned error: {}", advisor.getName(), context.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("Error in after advisor {}", advisor.getName(), e);
                context.setError(true);
                context.setErrorMessage("Advisor後置處理錯誤: " + e.getMessage());
            }
        }

        return context;
    }

    /**
     * 獲取所有已註冊的Advisor
     *
     * @return Advisor列表
     */
    private List<Advisor> getAdvisors() {
        if (advisors == null) {
            // 從Spring上下文獲取所有Advisor實現
            advisors = applicationContext.getBeansOfType(Advisor.class).values()
                .stream()
                .sorted(Comparator.comparingInt(Advisor::getOrder))
                .collect(Collectors.toList());

            log.info("Loaded {} advisors", advisors.size());
            advisors.forEach(advisor ->
                log.info("  - {} (order: {})", advisor.getName(), advisor.getOrder())
            );
        }

        return advisors;
    }

    /**
     * 刷新Advisor列表緩存
     *
     * 當新增或移除Advisor時調用
     */
    public void refreshAdvisors() {
        log.info("Refreshing advisor list");
        advisors = null;
        getAdvisors();
    }

    /**
     * 獲取特定的Advisor
     *
     * @param name Advisor名稱
     * @return Advisor實例，如果不存在則返回null
     */
    public Advisor getAdvisor(String name) {
        return getAdvisors().stream()
            .filter(advisor -> advisor.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    /**
     * 獲取所有已註冊的Advisor信息
     *
     * @return Advisor信息列表
     */
    public List<Map<String, Object>> getAdvisorsInfo() {
        return getAdvisors().stream()
            .map(advisor -> {
                Map<String, Object> info = new HashMap<>();
                info.put("name", advisor.getName());
                info.put("order", advisor.getOrder());
                info.put("class", advisor.getClass().getSimpleName());
                return info;
            })
            .collect(Collectors.toList());
    }
}
