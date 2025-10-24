package com.example.service;

import com.example.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 結構化分析服務
 * 提供企業級的結構化資料轉換和分析功能
 * 對應 5.10 章節的企業級應用場景實現
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StructuredAnalysisService {

    private final ChatModel chatModel;

    /**
     * 銷售資料分析
     * @param salesData 銷售資料
     * @return 結構化分析結果
     */
    public SalesAnalysisResult analyzeSalesData(Map<String, Object> salesData) {
        try {
            log.info("開始銷售資料分析");

            String userMessage = String.format(
                "分析以下銷售資料並提供洞察：\n%s\n\n" +
                "請提供結構化的分析，包括：\n" +
                "1. 銷售趨勢分析\n" +
                "2. 產品表現評估\n" +
                "3. 市場機會識別\n" +
                "4. 改進建議\n" +
                "5. 風險評估", salesData);

            SalesAnalysisResult result = ChatClient.create(chatModel)
                    .prompt()
                    .system("你是一個專業的銷售資料分析師，請提供準確且結構化的分析結果。")
                    .user(userMessage)
                    .call()
                    .entity(SalesAnalysisResult.class);

            log.info("銷售資料分析完成，趨勢：{}", result.trend());

            return result;

        } catch (Exception e) {
            log.error("銷售資料分析失敗", e);
            throw new RuntimeException("銷售資料分析失敗：" + e.getMessage());
        }
    }

    /**
     * 客戶行為分析
     * @param customerData 客戶資料
     * @return 客戶洞察
     */
    public CustomerInsights analyzeCustomerBehavior(List<Map<String, Object>> customerData) {
        try {
            log.info("開始客戶行為分析，資料筆數：{}", customerData.size());

            String userMessage = String.format(
                "分析以下客戶行為資料：\n%s\n\n" +
                "請提供結構化的客戶洞察，包括：\n" +
                "1. 客戶細分\n" +
                "2. 行為模式\n" +
                "3. 偏好分析\n" +
                "4. 流失風險\n" +
                "5. 個性化建議", customerData);

            CustomerInsights result = ChatClient.create(chatModel)
                    .prompt()
                    .system("你是一個客戶行為分析專家，請基於資料提供深入的客戶洞察。")
                    .user(userMessage)
                    .call()
                    .entity(CustomerInsights.class);

            log.info("客戶行為分析完成，識別出 {} 個客戶群體",
                    result.segments().size());

            return result;

        } catch (Exception e) {
            log.error("客戶行為分析失敗", e);
            throw new RuntimeException("客戶行為分析失敗：" + e.getMessage());
        }
    }

    /**
     * 市場趨勢預測
     * @param marketData 市場資料
     * @param timeframe 預測時間範圍
     * @return 市場預測結果
     */
    public MarketForecast predictMarketTrends(Map<String, Object> marketData, String timeframe) {
        try {
            log.info("開始市場趨勢預測，時間範圍：{}", timeframe);

            String userMessage = String.format(
                "基於以下市場資料預測 %s 的市場趨勢：\n%s\n\n" +
                "請提供結構化的預測結果，包括：\n" +
                "1. 整體市場趨勢\n" +
                "2. 關鍵指標預測\n" +
                "3. 機會與威脅\n" +
                "4. 策略建議\n" +
                "5. 預測信心度", timeframe, marketData);

            MarketForecast result = ChatClient.create(chatModel)
                    .prompt()
                    .system("你是一個市場分析和預測專家，請基於歷史資料提供準確的市場預測。")
                    .user(userMessage)
                    .call()
                    .entity(MarketForecast.class);

            log.info("市場趨勢預測完成，整體趨勢：{}，信心度：{}",
                    result.overallTrend(), result.confidenceLevel());

            return result;

        } catch (Exception e) {
            log.error("市場趨勢預測失敗", e);
            throw new RuntimeException("市場趨勢預測失敗：" + e.getMessage());
        }
    }
}
