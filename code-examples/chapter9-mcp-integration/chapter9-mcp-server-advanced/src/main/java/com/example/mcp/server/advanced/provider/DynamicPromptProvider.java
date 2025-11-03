package com.example.mcp.server.advanced.provider;

import com.example.mcp.server.advanced.entity.PromptTemplate;
import com.example.mcp.server.advanced.repository.PromptTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 動態提示提供者
 * 從資料庫載入提示模板並動態生成提示
 */
@Service
@Slf4j
public class DynamicPromptProvider {

    @Autowired
    private PromptTemplateRepository promptTemplateRepository;

    /**
     * 資料庫驅動的動態查詢提示
     * 根據類別從資料庫載入提示模板
     *
     * @param exchange MCP Server Exchange
     * @param category 查詢類別（如 tech, travel）
     * @return 動態生成的提示結果
     */
    @McpPrompt(
        name = "dynamic-query",
        description = "Database-driven dynamic prompt based on category"
    )
    public McpSchema.GetPromptResult dynamicQuery(
            McpSyncServerExchange exchange,
            @McpArg(name = "category", required = true,
                    description = "Query category (tech, travel, etc.)") String category) {

        log.info("載入動態提示: category={}", category);

        // 記錄開始載入
        exchange.loggingNotification(McpSchema.LoggingMessageNotification.builder()
            .level(McpSchema.LoggingLevel.INFO)
            .data("Loading dynamic prompt template for category: " + category)
            .build());

        // 從資料庫查找模板
        String templateName = "query-" + category.toLowerCase();
        Optional<PromptTemplate> templateOpt = promptTemplateRepository.findByName(templateName);

        if (templateOpt.isEmpty()) {
            log.warn("提示模板不存在: {}", templateName);
            exchange.loggingNotification(McpSchema.LoggingMessageNotification.builder()
                .level(McpSchema.LoggingLevel.WARNING)
                .data("Template not found: " + templateName)
                .build());

            return createErrorPrompt("Template not found for category: " + category);
        }

        PromptTemplate template = templateOpt.get();

        // 記錄成功載入
        exchange.loggingNotification(McpSchema.LoggingMessageNotification.builder()
            .level(McpSchema.LoggingLevel.INFO)
            .data(String.format("Template loaded: %s (parameters: %s)",
                template.getName(), template.getParameters()))
            .build());

        // 使用模板內容建立提示
        String content = template.getTemplate();

        return new McpSchema.GetPromptResult(
            "Dynamic Query - " + category,
            List.of(new McpSchema.PromptMessage(
                McpSchema.Role.ASSISTANT,
                new McpSchema.TextContent(content)
            ))
        );
    }

    /**
     * 根據模板名稱取得提示
     * 允許直接通過模板名稱載入提示
     *
     * @param exchange MCP Server Exchange
     * @param templateName 模板名稱
     * @param params 參數值（JSON格式字串，可選）
     * @return 提示結果
     */
    @McpPrompt(
        name = "template-prompt",
        description = "Load prompt from database template by name"
    )
    public McpSchema.GetPromptResult templatePrompt(
            McpSyncServerExchange exchange,
            @McpArg(name = "templateName", required = true,
                    description = "Template name to load") String templateName,
            @McpArg(name = "params", required = false,
                    description = "Template parameters (optional)") String params) {

        log.info("載入提示模板: templateName={}, params={}", templateName, params);

        Optional<PromptTemplate> templateOpt = promptTemplateRepository.findByName(templateName);

        if (templateOpt.isEmpty()) {
            log.warn("提示模板不存在: {}", templateName);
            return createErrorPrompt("Template not found: " + templateName);
        }

        PromptTemplate template = templateOpt.get();

        // 記錄模板資訊
        exchange.loggingNotification(McpSchema.LoggingMessageNotification.builder()
            .level(McpSchema.LoggingLevel.DEBUG)
            .data(String.format("Template: %s, Description: %s",
                template.getName(), template.getDescription()))
            .build());

        String content = template.getTemplate();

        // 如果有參數,進行簡單替換（實際應用中可能需要更複雜的模板引擎）
        if (params != null && !params.isEmpty()) {
            log.debug("處理參數替換: {}", params);
            // 這裡可以添加參數解析和替換邏輯
            // 例如: content = processTemplateParameters(content, params);
        }

        return new McpSchema.GetPromptResult(
            template.getDescription() != null ? template.getDescription() : template.getName(),
            List.of(new McpSchema.PromptMessage(
                McpSchema.Role.USER,
                new McpSchema.TextContent(content)
            ))
        );
    }

    /**
     * 列出所有可用的動態提示模板
     *
     * @param exchange MCP Server Exchange
     * @return 包含所有模板列表的提示
     */
    @McpPrompt(
        name = "list-templates",
        description = "List all available prompt templates from database"
    )
    public McpSchema.GetPromptResult listTemplates(McpSyncServerExchange exchange) {

        log.info("列出所有提示模板");

        List<PromptTemplate> templates = promptTemplateRepository.findAll();

        StringBuilder content = new StringBuilder();
        content.append("# Available Prompt Templates\n\n");

        if (templates.isEmpty()) {
            content.append("No templates found in database.\n");
        } else {
            for (PromptTemplate template : templates) {
                content.append(String.format("## %s\n", template.getName()));
                content.append(String.format("- **Description**: %s\n", template.getDescription()));
                if (!template.getParameters().isEmpty()) {
                    content.append(String.format("- **Parameters**: %s\n",
                        String.join(", ", template.getParameters())));
                }
                content.append("\n");
            }
        }

        exchange.loggingNotification(McpSchema.LoggingMessageNotification.builder()
            .level(McpSchema.LoggingLevel.INFO)
            .data(String.format("Found %d templates", templates.size()))
            .build());

        return new McpSchema.GetPromptResult(
            "Template List",
            List.of(new McpSchema.PromptMessage(
                McpSchema.Role.ASSISTANT,
                new McpSchema.TextContent(content.toString())
            ))
        );
    }

    /**
     * 建立錯誤提示
     *
     * @param errorMessage 錯誤訊息
     * @return 錯誤提示結果
     */
    private McpSchema.GetPromptResult createErrorPrompt(String errorMessage) {
        return new McpSchema.GetPromptResult(
            "Error",
            List.of(new McpSchema.PromptMessage(
                McpSchema.Role.ASSISTANT,
                new McpSchema.TextContent("Error: " + errorMessage)
            ))
        );
    }
}
