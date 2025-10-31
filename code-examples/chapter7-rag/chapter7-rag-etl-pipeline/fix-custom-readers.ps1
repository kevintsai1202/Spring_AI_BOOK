# 修正 ImageOCRDocumentReader.java
$file1 = 'src\main\java\com\example\etl\reader\ImageOCRDocumentReader.java'
$content1 = Get-Content $file1 -Raw -Encoding UTF8

# 將 get() 改名為 read() 並添加 get() 方法
$content1 = $content1 -replace '@Override\s+public List<Document> get\(\) \{', '@Override
    public List<Document> read() {'

# 添加 get() 方法委託給 read()
$insertPos = $content1.IndexOf('    @Override')
$beforeOverride = $content1.Substring(0, $insertPos)
$afterOverride = $content1.Substring($insertPos)
$content1 = $beforeOverride + '    @Override
    public List<Document> get() {
        return read();
    }

' + $afterOverride

$content1 | Set-Content $file1 -Encoding UTF8 -NoNewline

# 修正 ArchiveDocumentReader.java  
$file2 = 'src\main\java\com\example\etl\reader\ArchiveDocumentReader.java'
$content2 = Get-Content $file2 -Raw -Encoding UTF8

# 將 get() 改名為 read() 並添加 get() 方法
$content2 = $content2 -replace '@Override\s+public List<Document> get\(\) \{', '@Override
    public List<Document> read() {'

# 添加 get() 方法委託給 read()
$insertPos2 = $content2.IndexOf('    @Override')
$beforeOverride2 = $content2.Substring(0, $insertPos2)
$afterOverride2 = $content2.Substring($insertPos2)
$content2 = $beforeOverride2 + '    @Override
    public List<Document> get() {
        return read();
    }

' + $afterOverride2

# 修正內部呼叫
$content2 = $content2 -replace 'List<Document> documents = reader\.get\(\);', 'List<Document> documents = reader.read();'

$content2 | Set-Content $file2 -Encoding UTF8 -NoNewline

Write-Host "已修正自訂 DocumentReader 類別"
