package com.example.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工具註冊表 - 管理和發現所有可用的 Tool Calling 工具
 * 負責工具的自動註冊、查詢和執行管理
 */
@Slf4j
@Component
public class ToolRegistry {

    /**
     * 工具元數據 - 包含工具的完整信息
     */
    @Getter
    @RequiredArgsConstructor
    public static class ToolMetadata {
        private final String name;              // 工具名稱
        private final String description;       // 工具描述
        private final String category;          // 工具分類
        private final Object instance;          // 工具實例
        private final Method method;            // 工具執行方法
        private final Map<String, String> params; // 參數說明
    }

    /**
     * 內部工具存儲結構
     */
    @Getter
    @RequiredArgsConstructor
    private static class ToolDefinition {
        private final ToolMetadata metadata;
        private final long registeredTime;
    }

    // 按名稱存儲工具
    private final Map<String, ToolDefinition> toolsByName = new HashMap<>();

    // 按分類存儲工具
    private final Map<String, List<ToolDefinition>> toolsByCategory = new HashMap<>();

    // 工具執行歷史 (用於監控和分析)
    private final List<ToolExecutionRecord> executionHistory = Collections.synchronizedList(
            new ArrayList<>()
    );

    /**
     * 工具執行記錄 - 用於監控和性能分析
     */
    @Getter
    @RequiredArgsConstructor
    public static class ToolExecutionRecord {
        private final String toolName;
        private final long executionTimeMs;
        private final boolean success;
        private final String errorMessage;
        private final long timestamp;
    }

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * 初始化時自動發現和註冊所有工具
     */
    public void init() {
        log.info("開始初始化工具註冊表...");

        // 掃描所有組件
        String[] beanNames = applicationContext.getBeanNamesForAnnotation(
                org.springframework.stereotype.Component.class
        );

        for (String beanName : beanNames) {
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();

            // 掃描類中的所有方法
            for (Method method : beanClass.getMethods()) {
                if (isToolMethod(method)) {
                    registerTool(bean, method);
                }
            }
        }

        log.info("工具註冊表初始化完成，共註冊 {} 個工具", toolsByName.size());
    }

    /**
     * 判斷方法是否為工具方法（帶有特定註解）
     */
    private boolean isToolMethod(Method method) {
        // 檢查方法是否有 @AiFunction 或類似註解
        // 這裡簡化處理，實際應該檢查具體的工具註解
        return method.getName().endsWith("Tool") || method.getName().startsWith("call");
    }

    /**
     * 註冊單個工具
     */
    public void registerTool(Object instance, Method method) {
        String toolName = extractToolName(method);
        String description = extractToolDescription(method);
        String category = extractToolCategory(instance.getClass());

        // 創建工具元數據
        ToolMetadata metadata = new ToolMetadata(
                toolName,
                description,
                category,
                instance,
                method,
                extractParameters(method)
        );

        // 存儲工具
        ToolDefinition definition = new ToolDefinition(metadata, System.currentTimeMillis());
        toolsByName.put(toolName, definition);

        // 按分類存儲
        toolsByCategory.computeIfAbsent(category, k -> new ArrayList<>()).add(definition);

        log.debug("已註冊工具：[{}] - {}", toolName, description);
    }

    /**
     * 從方法名提取工具名稱
     */
    private String extractToolName(Method method) {
        String name = method.getName();
        // 移除 "Tool" 或 "call" 前後綴
        if (name.endsWith("Tool")) {
            name = name.substring(0, name.length() - 4);
        } else if (name.startsWith("call")) {
            name = name.substring(4);
        }
        return name;
    }

    /**
     * 從方法的 Javadoc 或註解提取描述
     */
    private String extractToolDescription(Method method) {
        // 實現中可以讀取註解或 Javadoc
        return "Tool: " + method.getName();
    }

    /**
     * 從類名提取工具分類
     */
    private String extractToolCategory(Class<?> clazz) {
        String className = clazz.getSimpleName();
        // 例如 DateTimeTools -> DATETIME
        if (className.contains("DateTime")) {
            return "DATETIME";
        } else if (className.contains("Calculator")) {
            return "CALCULATOR";
        } else if (className.contains("Product")) {
            return "PRODUCT";
        } else if (className.contains("Weather")) {
            return "WEATHER";
        } else if (className.contains("Sales")) {
            return "SALES";
        }
        return "GENERAL";
    }

    /**
     * 提取方法參數信息
     */
    private Map<String, String> extractParameters(Method method) {
        Map<String, String> params = new LinkedHashMap<>();
        // 實現中可以讀取參數註解或反射獲取參數類型
        return params;
    }

    /**
     * 根據名稱獲取工具
     */
    public Optional<ToolMetadata> getTool(String toolName) {
        ToolDefinition definition = toolsByName.get(toolName);
        return Optional.ofNullable(definition).map(ToolDefinition::getMetadata);
    }

    /**
     * 根據分類獲取所有工具
     */
    public List<ToolMetadata> getToolsByCategory(String category) {
        return toolsByCategory.getOrDefault(category, Collections.emptyList())
                .stream()
                .map(ToolDefinition::getMetadata)
                .collect(Collectors.toList());
    }

    /**
     * 獲取所有已註冊的工具
     */
    public List<ToolMetadata> getAllTools() {
        return toolsByName.values()
                .stream()
                .map(ToolDefinition::getMetadata)
                .collect(Collectors.toList());
    }

    /**
     * 獲取所有工具分類
     */
    public Set<String> getAllCategories() {
        return new HashSet<>(toolsByCategory.keySet());
    }

    /**
     * 執行工具並記錄
     */
    public Object executeTool(String toolName, Object... args) throws Exception {
        Optional<ToolMetadata> toolOpt = getTool(toolName);

        if (toolOpt.isEmpty()) {
            throw new IllegalArgumentException("未找到工具：" + toolName);
        }

        ToolMetadata metadata = toolOpt.get();
        long startTime = System.currentTimeMillis();

        try {
            // 執行工具方法
            Object result = metadata.getMethod().invoke(metadata.getInstance(), args);

            // 記錄成功執行
            long executionTime = System.currentTimeMillis() - startTime;
            recordExecution(toolName, executionTime, true, null);

            log.debug("工具 [{}] 執行成功，耗時 {}ms", toolName, executionTime);
            return result;

        } catch (Exception e) {
            // 記錄失敗執行
            long executionTime = System.currentTimeMillis() - startTime;
            recordExecution(toolName, executionTime, false, e.getMessage());

            log.error("工具 [{}] 執行失敗", toolName, e);
            throw e;
        }
    }

    /**
     * 記錄工具執行情況
     */
    private void recordExecution(String toolName, long executionTimeMs,
                                 boolean success, String errorMessage) {
        ToolExecutionRecord record = new ToolExecutionRecord(
                toolName,
                executionTimeMs,
                success,
                errorMessage,
                System.currentTimeMillis()
        );
        executionHistory.add(record);

        // 保持歷史記錄在合理的大小
        if (executionHistory.size() > 1000) {
            executionHistory.remove(0);
        }
    }

    /**
     * 獲取工具執行統計
     */
    public Map<String, Object> getExecutionStats(String toolName) {
        List<ToolExecutionRecord> records = executionHistory.stream()
                .filter(r -> r.getToolName().equals(toolName))
                .collect(Collectors.toList());

        if (records.isEmpty()) {
            return Collections.emptyMap();
        }

        long totalTime = records.stream().mapToLong(ToolExecutionRecord::getExecutionTimeMs).sum();
        long successCount = records.stream().filter(ToolExecutionRecord::isSuccess).count();
        long failureCount = records.size() - successCount;

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalExecutions", records.size());
        stats.put("successCount", successCount);
        stats.put("failureCount", failureCount);
        stats.put("successRate", (double) successCount / records.size());
        stats.put("avgExecutionTimeMs", (double) totalTime / records.size());
        stats.put("maxExecutionTimeMs", records.stream().mapToLong(ToolExecutionRecord::getExecutionTimeMs).max().orElse(0));
        stats.put("minExecutionTimeMs", records.stream().mapToLong(ToolExecutionRecord::getExecutionTimeMs).min().orElse(0));

        return stats;
    }

    /**
     * 獲取所有工具統計
     */
    public Map<String, Map<String, Object>> getAllStats() {
        Map<String, Map<String, Object>> allStats = new LinkedHashMap<>();
        for (ToolMetadata tool : getAllTools()) {
            allStats.put(tool.getName(), getExecutionStats(tool.getName()));
        }
        return allStats;
    }

    /**
     * 清空執行歷史
     */
    public void clearHistory() {
        executionHistory.clear();
        log.info("已清空工具執行歷史");
    }

    /**
     * 獲取工具總數
     */
    public int getTotalToolCount() {
        return toolsByName.size();
    }
}
