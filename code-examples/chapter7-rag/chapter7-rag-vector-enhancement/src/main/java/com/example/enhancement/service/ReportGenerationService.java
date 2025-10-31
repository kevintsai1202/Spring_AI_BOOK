package com.example.enhancement.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 報告生成服務
 * 基於 RAG 查詢結果生成結構化報告
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportGenerationService {

    private final ChatModel chatModel;
    private final EnterpriseRAGService enterpriseRAGService;

    /**
     * 生成企業資料分析報告
     *
     * @param topic 報告主題
     * @param questions 分析問題列表
     * @return 生成的報告
     */
    public GeneratedReport generateReport(String topic, List<String> questions) {
        log.info("開始生成報告: topic={}, questions={}", topic, questions.size());

        GeneratedReport.GeneratedReportBuilder reportBuilder = GeneratedReport.builder()
                .topic(topic)
                .generatedAt(LocalDateTime.now());

        StringBuilder reportContent = new StringBuilder();

        // 1. 報告標題和摘要
        reportContent.append("# ").append(topic).append("\n\n");
        reportContent.append("**生成時間**: ").append(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ).append("\n\n");
        reportContent.append("---\n\n");

        // 2. 對每個問題執行 RAG 查詢並整理結果
        List<ReportSection> sections = questions.stream()
                .map(question -> generateReportSection(question))
                .collect(Collectors.toList());

        reportBuilder.sections(sections);

        // 3. 組裝報告內容
        for (int i = 0; i < sections.size(); i++) {
            ReportSection section = sections.get(i);
            reportContent.append("## ").append(i + 1).append(". ").append(section.getQuestion()).append("\n\n");
            reportContent.append(section.getAnswer()).append("\n\n");

            if (!section.getSourceSummary().isEmpty()) {
                reportContent.append("**資料來源**:\n");
                section.getSourceSummary().forEach(source ->
                        reportContent.append("- ").append(source).append("\n")
                );
                reportContent.append("\n");
            }
        }

        // 4. 生成執行摘要
        String executiveSummary = generateExecutiveSummary(topic, sections);
        reportBuilder.executiveSummary(executiveSummary);

        // 在報告開頭添加執行摘要
        StringBuilder finalReport = new StringBuilder();
        finalReport.append("# ").append(topic).append("\n\n");
        finalReport.append("**生成時間**: ").append(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ).append("\n\n");
        finalReport.append("## 執行摘要\n\n");
        finalReport.append(executiveSummary).append("\n\n");
        finalReport.append("---\n\n");
        finalReport.append("## 詳細分析\n\n");
        finalReport.append(reportContent.toString().split("---\\n\\n", 2)[1]);

        reportBuilder.content(finalReport.toString());

        log.info("報告生成完成: sections={}, length={}", sections.size(), finalReport.length());
        return reportBuilder.build();
    }

    /**
     * 生成報告段落
     */
    private ReportSection generateReportSection(String question) {
        log.debug("處理報告問題: {}", question);

        // 使用 RAG 查詢獲取答案
        EnterpriseRAGService.RAGQueryResult ragResult =
                enterpriseRAGService.query(question, 5, 0.7);

        // 提取來源摘要
        List<String> sourceSummary = ragResult.getSources().stream()
                .map(source -> String.format("%s: %s", source.getType(), source.getName()))
                .distinct()
                .collect(Collectors.toList());

        return ReportSection.builder()
                .question(question)
                .answer(ragResult.getAnswer())
                .sourceSummary(sourceSummary)
                .documentsUsed(ragResult.getRetrievedDocuments())
                .build();
    }

    /**
     * 生成執行摘要
     */
    private String generateExecutiveSummary(String topic, List<ReportSection> sections) {
        log.debug("生成執行摘要");

        // 組裝所有問答對
        String sectionsText = sections.stream()
                .map(section -> "問題: " + section.getQuestion() + "\n答案: " + section.getAnswer())
                .collect(Collectors.joining("\n\n"));

        // 使用 AI 生成執行摘要
        String promptText = "基於以下的分析結果，請生成一個簡潔的執行摘要（約200-300字）。\n" +
                "執行摘要應該：\n" +
                "1. 總結主要發現\n" +
                "2. 突出關鍵洞察\n" +
                "3. 提供可行的建議\n\n" +
                "主題: {topic}\n\n" +
                "分析內容:\n" +
                "{sections}\n\n" +
                "請用繁體中文撰寫執行摘要：";

        PromptTemplate promptTemplate = new PromptTemplate(promptText);
        Map<String, Object> params = new HashMap<>();
        params.put("topic", topic);
        params.put("sections", sectionsText);

        Prompt prompt = promptTemplate.create(params);
        ChatResponse response = chatModel.call(prompt);

        return response.getResult().getOutput().getText();
    }

    /**
     * 生成快速報告（單一問題）
     */
    public String generateQuickReport(String question) {
        log.info("生成快速報告: {}", question);

        EnterpriseRAGService.RAGQueryResult result =
                enterpriseRAGService.query(question, 5, 0.7);

        StringBuilder report = new StringBuilder();
        report.append("# 快速查詢報告\n\n");
        report.append("**問題**: ").append(question).append("\n\n");
        report.append("**生成時間**: ").append(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ).append("\n\n");
        report.append("---\n\n");
        report.append("## 分析結果\n\n");
        report.append(result.getAnswer()).append("\n\n");

        if (!result.getSources().isEmpty()) {
            report.append("## 資料來源\n\n");
            result.getSources().forEach(source ->
                    report.append(String.format("- **%s**: %s\n",
                            source.getType(), source.getName()))
            );
        }

        return report.toString();
    }

    /**
     * 生成的報告
     */
    @Data
    @Builder
    public static class GeneratedReport {
        /**
         * 報告主題
         */
        private String topic;

        /**
         * 執行摘要
         */
        private String executiveSummary;

        /**
         * 報告內容（Markdown 格式）
         */
        private String content;

        /**
         * 報告段落列表
         */
        private List<ReportSection> sections;

        /**
         * 生成時間
         */
        private LocalDateTime generatedAt;
    }

    /**
     * 報告段落
     */
    @Data
    @Builder
    @AllArgsConstructor
    public static class ReportSection {
        /**
         * 問題
         */
        private String question;

        /**
         * 答案
         */
        private String answer;

        /**
         * 來源摘要
         */
        private List<String> sourceSummary;

        /**
         * 使用的文檔數量
         */
        private int documentsUsed;
    }
}
