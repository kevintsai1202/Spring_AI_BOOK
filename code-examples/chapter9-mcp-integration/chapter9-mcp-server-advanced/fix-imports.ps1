# 修正 MCP 導入路徑腳本

$providerFiles = @(
    "src\main\java\com\example\mcp\server\advanced\provider\PromptProvider.java",
    "src\main\java\com\example\mcp\server\advanced\provider\DynamicPromptProvider.java",
    "src\main\java\com\example\mcp\server\advanced\provider\CompletionProvider.java"
)

$replacements = @{
    "org.springframework.ai.mcp.spec.McpSchema" = "io.modelcontextprotocol.spec.McpSchema"
    "org.springframework.ai.mcp.spec.ServerMcpTransport" = "io.modelcontextprotocol.spec.McpSchema"
    "org.springframework.ai.mcp.spring.McpPrompt" = "org.springaicommunity.mcp.annotation.McpPrompt"
    "org.springframework.ai.mcp.spring.McpArg" = "org.springaicommunity.mcp.annotation.McpArg"
    "org.springframework.ai.mcp.spring.McpComplete" = "org.springaicommunity.mcp.annotation.McpComplete"
    "org.springframework.ai.mcp.spring.McpSyncServerExchange" = "io.modelcontextprotocol.server.McpSyncServerExchange"
    "ServerMcpTransport.TextContent" = "McpSchema.TextContent"
}

Write-Host "開始修正導入路徑..." -ForegroundColor Green

foreach ($file in $providerFiles) {
    $fullPath = "E:\Spring_AI_BOOK\code-examples\chapter9-mcp-integration\chapter9-mcp-server-advanced\$file"

    if (Test-Path $fullPath) {
        Write-Host "處理: $file" -ForegroundColor Cyan

        $content = Get-Content $fullPath -Raw -Encoding UTF8

        foreach ($old in $replacements.Keys) {
            $new = $replacements[$old]
            $content = $content -replace [regex]::Escape($old), $new
        }

        Set-Content -Path $fullPath -Value $content -Encoding UTF8 -NoNewline
        Write-Host "  ✓ 已更新" -ForegroundColor Green
    } else {
        Write-Host "  ✗ 檔案不存在: $file" -ForegroundColor Red
    }
}

Write-Host "`n修正完成！" -ForegroundColor Green
