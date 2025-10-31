package com.example.memory.advisor;

/**
 * Advisor 增強器介面
 *
 * 定義了責任鏈模式中的Advisor規範
 * Advisor 可以在請求前和回應後進行攔截和修改
 */
public interface Advisor {

    /**
     * 獲取Advisor的名稱
     *
     * @return Advisor名稱
     */
    String getName();

    /**
     * 獲取執行順序
     *
     * 數字越小越先執行，允許負數
     * 推薦的順序：
     * - SecurityAdvisor: -1000 (最先執行，安全檢查)
     * - MemoryAdvisor: 0 (基礎功能)
     * - FilterAdvisor: 100 (內容過濾)
     * - LoggingAdvisor: 800 (日誌記錄)
     * - MetricsAdvisor: 900 (統計指標)
     *
     * @return 執行優先級
     */
    int getOrder();

    /**
     * 在請求前執行處理
     *
     * @param context Advisor上下文
     * @return 修改後的Advisor上下文
     */
    AdvisorContext adviseRequest(AdvisorContext context);

    /**
     * 在回應後執行處理
     *
     * @param context Advisor上下文
     * @return 修改後的Advisor上下文
     */
    AdvisorContext adviseResponse(AdvisorContext context);

    /**
     * 檢查Advisor是否支持該請求
     *
     * @param context Advisor上下文
     * @return 是否支持
     */
    default boolean supports(AdvisorContext context) {
        return true;
    }
}
