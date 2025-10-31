# 上傳測試文檔

$url = "http://localhost:8080/api/rag/documents"
$filePath = "test-document.txt"

Write-Host "上傳測試文檔: $filePath" -ForegroundColor Cyan

# 使用 Invoke-WebRequest 上傳文件
$fileBytes = [System.IO.File]::ReadAllBytes($filePath)
$fileContent = [System.Text.Encoding]::GetEncoding('UTF-8').GetString($fileBytes)

$boundary = [System.Guid]::NewGuid().ToString()
$ContentType = "multipart/form-data; boundary=$boundary"

$bodyLines = @(
    "--$boundary",
    'Content-Disposition: form-data; name="files"; filename="test-document.txt"',
    'Content-Type: text/plain',
    '',
    $fileContent,
    "--$boundary--"
) -join "`r`n"

try {
    $response = Invoke-WebRequest -Uri $url -Method Post -ContentType $ContentType -Body $bodyLines
    $result = $response.Content | ConvertFrom-Json

    if ($result.success) {
        Write-Host "✓ 上傳成功!" -ForegroundColor Green
        Write-Host "  訊息: $($result.message)" -ForegroundColor White
        Write-Host "  處理文檔數: $($result.documentsProcessed)" -ForegroundColor White
    } else {
        Write-Host "× 上傳失敗: $($result.message)" -ForegroundColor Red
    }
} catch {
    Write-Host "× 上傳失敗: $($_.Exception.Message)" -ForegroundColor Red
}
