[CmdletBinding()]
param()

# 1. Define Paths
$sourceDir = "E:\Spring_AI_BOOK\docs"
$outputDir = "E:\Spring_AI_BOOK\Word輸出"
$outputDocxFile = Join-Path -Path $outputDir -ChildPath "Spring_AI_Book_Generated.docx"
$mergedMdPath = Join-Path -Path $outputDir -ChildPath "merged.md"

# 2. Prepare Output Directory
if (-not (Test-Path -Path $outputDir)) {
    New-Item -Path $outputDir -ItemType Directory | Out-Null
}

# 3. Get Markdown files
$mdFiles = Get-ChildItem -Path $sourceDir -Recurse -Filter "*.md" | Where-Object { $_.Name -ne 'README.md' } | Sort-Object { $_.FullName }

# 4. Initialize counters and merged file
$imageCounter = 1
Set-Content -Path $mergedMdPath -Value ""

# Pandoc pagebreak string
$pageBreak = @"

\newpage

"@

# 5. Process each file
foreach ($file in $mdFiles) {
    Write-Host "Processing: $($file.FullName)"
    $content = Get-Content -Path $file.FullName -Raw

    # Process Mermaid blocks
    while ($content -match '(?s)```mermaid(.*?)```') {
        $mermaidCode = $matches[1]
        $tempMmd = New-TemporaryFile

        try {
            Set-Content -Path $tempMmd.FullName -Value $mermaidCode
            
            $imageFileName = "figure-$imageCounter.png"
            $imageFullPath = Join-Path -Path $outputDir -ChildPath $imageFileName
            
            Write-Host "  - Converting Mermaid diagram to $imageFileName..."
            mmdc -i $tempMmd.FullName -o $imageFullPath
            
            if (Test-Path $imageFullPath) {
                $replacement = "![]($imageFileName)"
                $content = $content -replace '(?s)```mermaid.*?```', $replacement, 1
                $imageCounter++
            } else {
                Write-Warning "  - Failed to convert Mermaid diagram."
                $content = $content -replace '(?s)```mermaid.*?```', "`[Mermaid diagram could not be rendered]`", 1
            }
        }
        finally {
            if ($tempMmd) {
                Remove-Item $tempMmd.FullName -ErrorAction SilentlyContinue
            }
        }
    }
    
    # Append content to merged file
    Add-Content -Path $mergedMdPath -Value $content
    Add-Content -Path $mergedMdPath -Value $pageBreak
}

# 6. Final Pandoc Conversion
Write-Host "Generating DOCX file..."
pandoc $mergedMdPath -o $outputDocxFile --from markdown --to docx --toc --resource-path=$outputDir

# 7. Cleanup
Remove-Item $mergedMdPath -ErrorAction SilentlyContinue

Write-Host "Success! File generated at: $outputDocxFile"