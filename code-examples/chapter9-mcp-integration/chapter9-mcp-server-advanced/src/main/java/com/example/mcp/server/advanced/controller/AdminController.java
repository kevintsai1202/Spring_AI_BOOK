package com.example.mcp.server.advanced.controller;

import com.example.mcp.server.advanced.service.DynamicToolManager;
import com.example.mcp.server.advanced.provider.CompletionProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理控制器
 * 提供管理 API 用於觸發動態工具更新等操作
 */
@RestController
@RequestMapping("/api/admin")
@Slf4j
public class AdminController {

    @Autowired
    private DynamicToolManager dynamicToolManager;

    @Autowired
    private CompletionProvider completionProvider;

    /**
     * 觸發動態工具更新
     *
     * @return 操作結果
     */
    @PostMapping("/update-tools")
    public ResponseEntity<Map<String, String>> updateTools() {
        log.info("收到動態工具更新請求");

        try {
            dynamicToolManager.triggerUpdate();

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "動態工具更新已觸發，新工具將在幾秒後可用"
            ));

        } catch (Exception e) {
            log.error("觸發工具更新失敗", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", "工具更新失敗: " + e.getMessage()
                ));
        }
    }

    /**
     * 檢查工具狀態
     *
     * @return 工具狀態資訊
     */
    @GetMapping("/tools/status")
    public ResponseEntity<Map<String, Object>> getToolsStatus() {
        log.info("查詢工具狀態");

        boolean toolsAdded = dynamicToolManager.areToolsAdded();

        return ResponseEntity.ok(Map.of(
            "status", "success",
            "toolsAdded", toolsAdded,
            "message", toolsAdded ? "動態工具已添加" : "動態工具尚未添加"
        ));
    }

    /**
     * 重新載入補全數據快取
     *
     * @return 操作結果
     */
    @PostMapping("/reload-completions")
    public ResponseEntity<Map<String, String>> reloadCompletions() {
        log.info("收到重新載入補全數據請求");

        try {
            completionProvider.reloadCache();

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "補全數據快取已重新載入"
            ));

        } catch (Exception e) {
            log.error("重新載入補全數據失敗", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "status", "error",
                    "message", "重新載入失敗: " + e.getMessage()
                ));
        }
    }

    /**
     * 健康檢查端點
     *
     * @return 健康狀態
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "MCP Server Advanced",
            "version", "1.0.0"
        ));
    }
}
