# RAG Vector Enhancement API 測試腳本

$baseUrl = "http://localhost:8080"
$username = "user"
$password = "15530009-77a9-406d-b309-134da2d318c5"

# 建立認證標頭
$pair = "${username}:${password}"
$bytes = [System.Text.Encoding]::ASCII.GetBytes($pair)
$base64 = [System.Convert]::ToBase64String($bytes)
$headers = @{
    "Authorization" = "Basic $base64"
    "Content-Type" = "application/json"
}

Write-Host "================================" -ForegroundColor Cyan
Write-Host "RAG Vector Enhancement API 測試" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# 1. 測試健康檢查
Write-Host "[1] 測試健康檢查 API..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/rag/health" -Method GET -Headers $headers
    Write-Host "✓ 健康檢查成功" -ForegroundColor Green
    Write-Host "  狀態: $($response.status)" -ForegroundColor Gray
    Write-Host "  訊息: $($response.message)" -ForegroundColor Gray
} catch {
    Write-Host "✗ 健康檢查失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 2. 測試企業資料初始化（索引所有資料）
Write-Host "[2] 測試企業資料初始化 API..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/enterprise/index/all" -Method POST -Headers $headers
    Write-Host "✓ 資料索引成功" -ForegroundColor Green
    Write-Host "  索引部門數: $($response.departmentsIndexed)" -ForegroundColor Gray
    Write-Host "  索引員工數: $($response.employeesIndexed)" -ForegroundColor Gray
    Write-Host "  索引產品數: $($response.productsIndexed)" -ForegroundColor Gray
    Write-Host "  索引專案數: $($response.projectsIndexed)" -ForegroundColor Gray
    Write-Host "  總計: $($response.totalDocuments)" -ForegroundColor Gray
} catch {
    Write-Host "✗ 資料索引失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 3. 測試企業資料統計
Write-Host "[3] 測試企業資料統計 API..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/enterprise/statistics" -Method GET -Headers $headers
    Write-Host "✓ 取得統計資料成功" -ForegroundColor Green
    Write-Host "  部門總數: $($response.totalDepartments)" -ForegroundColor Gray
    Write-Host "  員工總數: $($response.totalEmployees)" -ForegroundColor Gray
    Write-Host "  產品總數: $($response.totalProducts)" -ForegroundColor Gray
    Write-Host "  專案總數: $($response.totalProjects)" -ForegroundColor Gray
} catch {
    Write-Host "✗ 取得統計資料失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 4. 測試 RAG 查詢
Write-Host "[4] 測試 RAG 查詢 API..." -ForegroundColor Yellow
$ragQuery = @{
    query = "公司有哪些部門？"
    topK = 5
    similarityThreshold = 0.7
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/rag/query" -Method POST -Headers $headers -Body $ragQuery
    Write-Host "✓ RAG 查詢成功" -ForegroundColor Green
    Write-Host "  問題: $($response.query)" -ForegroundColor Gray
    Write-Host "  檢索文檔數: $($response.retrievedDocuments)" -ForegroundColor Gray
    Write-Host "  答案: $($response.answer.Substring(0, [Math]::Min(100, $response.answer.Length)))..." -ForegroundColor Gray
} catch {
    Write-Host "✗ RAG 查詢失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

# 5. 測試快速報告生成
Write-Host "[5] 測試快速報告生成 API..." -ForegroundColor Yellow
$quickReportRequest = @{
    question = "請分析公司的技術團隊情況"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/api/rag/report/quick" -Method POST -Headers $headers -Body $quickReportRequest
    Write-Host "✓ 快速報告生成成功" -ForegroundColor Green
    Write-Host "  問題: $($response.question)" -ForegroundColor Gray
    Write-Host "  格式: $($response.format)" -ForegroundColor Gray
    Write-Host "  內容長度: $($response.content.Length) 字元" -ForegroundColor Gray
} catch {
    Write-Host "✗ 快速報告生成失敗: $($_.Exception.Message)" -ForegroundColor Red
}
Write-Host ""

Write-Host "================================" -ForegroundColor Cyan
Write-Host "測試完成" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
