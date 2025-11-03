package com.example.mcpclient.model;

import lombok.Builder;
import lombok.Data;

/**
 * MCP Server 支援的功能能力
 *
 * 描述一個 MCP Server 可以提供哪些類型的功能。
 */
@Data
@Builder
public class ServerCapabilities {

    /**
     * 是否支援工具(Tools)
     * 工具是 Server 提供的可執行函數
     */
    @Builder.Default
    private boolean tools = false;

    /**
     * 是否支援資源(Resources)
     * 資源是 Server 提供的靜態或動態內容
     */
    @Builder.Default
    private boolean resources = false;

    /**
     * 是否支援提示(Prompts)
     * 提示是預定義的對話模板
     */
    @Builder.Default
    private boolean prompts = false;

    /**
     * 是否支援日誌(Logging)
     * 允許 Server 向 Client 發送日誌訊息
     */
    @Builder.Default
    private boolean logging = false;

    /**
     * 工具數量
     * 可用工具的總數
     */
    @Builder.Default
    private int toolCount = 0;

    /**
     * 資源數量
     * 可用資源的總數
     */
    @Builder.Default
    private int resourceCount = 0;
}
