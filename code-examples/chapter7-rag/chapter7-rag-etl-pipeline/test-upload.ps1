# 測試文件上傳 API

$baseUrl = "http://localhost:8081"
$testFile = "E:\Spring_AI_BOOK\code-examples\chapter7-rag\chapter7-rag-etl-pipeline\data\chapter7-rag-basic-spec.md"

Write-Host "測試 ETL Pipeline - 文件上傳功能`n"
Write-Host "測試文件: $testFile`n"

# 檢查文件是否存在
if (-not (Test-Path $testFile)) {
    Write-Host "錯誤: 測試文件不存在!" -ForegroundColor Red
    exit 1
}

# 準備上傳請求
$uri = "$baseUrl/api/etl/upload?chunkSize=1000&enableEnrichment=true"

Write-Host "發送請求到: $uri`n"

try {
    # 使用正確的 multipart form-data 格式
    $boundary = [System.Guid]::NewGuid().ToString()
    $LF = "`r`n"

    # 讀取文件內容
    $fileBytes = [System.IO.File]::ReadAllBytes($testFile)
    $fileName = [System.IO.Path]::GetFileName($testFile)

    # 建立 multipart body
    $bodyLines = (
        "--$boundary",
        "Content-Disposition: form-data; name=`"files`"; filename=`"$fileName`"",
        "Content-Type: application/octet-stream$LF",
        [System.Text.Encoding]::UTF8.GetString($fileBytes),
        "--$boundary--$LF"
    ) -join $LF

    # 發送請求
    $response = Invoke-RestMethod -Uri $uri -Method Post `
        -ContentType "multipart/form-data; boundary=$boundary" `
        -Body $bodyLines

    Write-Host "上傳成功!" -ForegroundColor Green
    Write-Host "`n響應內容:"
    $response | ConvertTo-Json -Depth 10

} catch {
    Write-Host "錯誤: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $stream = $_.Exception.Response.GetResponseStream()
        $reader = New-Object System.IO.StreamReader($stream)
        $responseBody = $reader.ReadToEnd()
        Write-Host "`n錯誤詳情:"
        Write-Host $responseBody
    }
}
