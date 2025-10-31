$file = 'src\main\java\com\example\etl\service\MultiFormatDocumentReader.java'
$content = Get-Content $file -Raw -Encoding UTF8

$content = $content -replace 'return pdfReader\.get\(\);', 'return pdfReader.read();'
$content = $content -replace 'List<Document> documents = tikaReader\.get\(\);', 'List<Document> documents = tikaReader.read();'
$content = $content -replace 'return textReader\.get\(\);', 'return textReader.read();'
$content = $content -replace 'return markdownReader\.get\(\);', 'return markdownReader.read();'
$content = $content -replace 'return jsonReader\.get\(\);', 'return jsonReader.read();'
$content = $content -replace 'return htmlReader\.get\(\);', 'return htmlReader.read();'
$content = $content -replace 'return ocrReader\.get\(\);', 'return ocrReader.read();'
$content = $content -replace 'return archiveReader\.get\(\);', 'return archiveReader.read();'

$content | Set-Content $file -Encoding UTF8 -NoNewline
Write-Host "已修正 MultiFormatDocumentReader.java"
