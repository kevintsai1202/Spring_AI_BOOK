
import os
import subprocess
import glob
import re
import urllib.request
import urllib.parse
import shutil
import ssl

# 0. Define full path to mmdc
mmdc_path = "C:\\Program Files\\nodejs\\mmdc.cmd"

# 1. Define Paths
source_dir = "E:\\Spring_AI_BOOK\\docs"
output_dir = "E:\\Spring_AI_BOOK\\Word輸出"
output_docx_file = os.path.join(output_dir, "Spring_AI_Book_Generated.docx")
merged_md_path = os.path.join(output_dir, "merged.md")
error_log_path = os.path.join(output_dir, "processing_errors.log")

# 2. Prepare Output Directory by clearing it first
print(f"Clearing and recreating output directory: {output_dir}")
if os.path.exists(output_dir):
    shutil.rmtree(output_dir)
os.makedirs(output_dir)

# 3. Get Markdown files from main chapter directories only
all_files = glob.glob(os.path.join(source_dir, "*", "*.md"))
md_files = sorted([f for f in all_files if 'README.md' not in os.path.basename(f)])

print(f"Found {len(md_files)} markdown files to process in main chapter directories.")

# 4. Process and merge
current_chapter_number = None
chapter_image_counter = 1
# 使用 Pandoc 的原始 HTML 語法來插入分頁符，適用於 DOCX 輸出
page_break = '\n\n<div style="page-break-after: always;"></div>\n\n'

with open(merged_md_path, 'w', encoding='utf-8') as merged_file:
    for file_path in md_files:
        print(f"Processing: {file_path}")

        chapter_match = re.search(r'chapter(\d+)', file_path)
        if chapter_match:
            new_chapter_number = chapter_match.group(1)
            is_new_chapter = (new_chapter_number != current_chapter_number)
            if is_new_chapter:
                # 如果不是第一個章節，在新章節開始前加入分頁符
                if current_chapter_number is not None:
                    merged_file.write(page_break)
                current_chapter_number = new_chapter_number
                chapter_image_counter = 1
        else:
            new_chapter_number = "0"
            is_new_chapter = (new_chapter_number != current_chapter_number)
            if is_new_chapter:
                if current_chapter_number is not None:
                    merged_file.write(page_break)
                current_chapter_number = new_chapter_number
                chapter_image_counter = 1

        try:
            with open(file_path, 'r', encoding='utf-8') as f:
                content = f.read()
        except Exception as e:
            print(f"  - ERROR: Could not read file {file_path}: {e}")
            continue

        # Use regex to find all mermaid blocks
        mermaid_blocks = re.findall('(?s)```mermaid(.*?)```', content)
        for mermaid_code in mermaid_blocks:
            temp_mmd_path = f"temp_mermaid_{current_chapter_number}_{chapter_image_counter}.mmd"
            with open(temp_mmd_path, 'w', encoding='utf-8') as temp_mmd:
                temp_mmd.write(mermaid_code)

            # New image naming convention: [chapter]-[sequence_number].png
            image_file_name = f"{current_chapter_number}-{chapter_image_counter}.png"
            image_full_path = os.path.join(output_dir, image_file_name)

            print(f"  - Converting Mermaid diagram to {image_file_name}...")
            try:
                subprocess.run([mmdc_path, '-i', temp_mmd_path, '-o', image_full_path], check=True, capture_output=True, text=True)
                replacement = f"{{{image_file_name}}}"
                content = content.replace(f"```mermaid{mermaid_code}```", replacement, 1)
                chapter_image_counter += 1
            except subprocess.CalledProcessError as e:
                error_message = e.stderr or "Unknown error"
                print(f"  - ERROR converting mermaid diagram: {error_message.strip()}")
                with open(error_log_path, 'a', encoding='utf-8') as log_file:
                    log_file.write(f"---\n--- Error in file: {file_path} ---\n")
                    log_file.write("Failed Mermaid Code:\n")
                    log_file.write(mermaid_code.strip() + "\n\n")
                    log_file.write("Error from mmdc:\n")
                    log_file.write(error_message + "\n")
                    log_file.write("-----------------------------------------\n\n")
                content = content.replace(f"```mermaid{mermaid_code}```", "[Mermaid diagram could not be rendered - see mermaid_errors.log]", 1)
            finally:
                if os.path.exists(temp_mmd_path):
                    os.remove(temp_mmd_path)

        # Process Markdown image links: ![alt](url)
        image_links = re.findall(r'!\[([^\]]*)\]\(([^)]+)\)', content)
        for alt_text, image_url in image_links:
            image_file_name = f"{current_chapter_number}-{chapter_image_counter}.png"
            image_full_path = os.path.join(output_dir, image_file_name)

            print(f"  - Downloading image from {image_url} to {image_file_name}...")
            try:
                # 判斷是網路 URL 還是本地路徑
                if image_url.startswith(('http://', 'https://')):
                    # 下載網路圖片（添加 headers 模擬瀏覽器請求，避免 403 錯誤）
                    ssl_context = ssl._create_unverified_context()
                    headers = {
                        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36',
                        'Referer': 'https://ithelp.ithome.com.tw/',
                        'Accept': 'image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8',
                        'Accept-Language': 'zh-TW,zh;q=0.9,en-US;q=0.8,en;q=0.7'
                    }
                    request = urllib.request.Request(image_url, headers=headers)
                    with urllib.request.urlopen(request, context=ssl_context) as response:
                        with open(image_full_path, 'wb') as out_file:
                            out_file.write(response.read())
                else:
                    # 複製本地圖片
                    local_image_path = os.path.join(os.path.dirname(file_path), image_url)
                    if os.path.exists(local_image_path):
                        shutil.copy(local_image_path, image_full_path)
                    else:
                        print(f"  - WARNING: Local image not found: {local_image_path}")
                        continue

                # 替換圖片連結為本地檔名
                original_markdown = f"![{alt_text}]({image_url})"
                replacement = f"{{{image_file_name}}}"
                content = content.replace(original_markdown, replacement, 1)
                chapter_image_counter += 1

            except Exception as e:
                print(f"  - ERROR downloading image from {image_url}: {str(e)}")
                with open(error_log_path, 'a', encoding='utf-8') as log_file:
                    log_file.write(f"---\n--- Error in file: {file_path} ---\n")
                    log_file.write(f"Failed to download image: {image_url}\n")
                    log_file.write(f"Error: {str(e)}\n")
                    log_file.write("-----------------------------------------\n\n")

        # 寫入內容（不再在每個檔案後加分頁符，只在章節變更時加）
        merged_file.write(content)
        # 在檔案之間加入適當的間隔
        merged_file.write("\n\n")

# 6. Pandoc Conversion
print("Generating DOCX file...")
try:
    subprocess.run(['pandoc', merged_md_path, '-o', output_docx_file, '--from', 'markdown', '--to', 'docx', '--toc', '--toc-depth', '1', '--resource-path', output_dir], check=True, capture_output=True, text=True)
    print(f"Success! File generated at: {output_docx_file}")
except subprocess.CalledProcessError as e:
    print(f"  - ERROR during Pandoc conversion: {e.stderr}")

# 7. Cleanup
finally:
    if os.path.exists(merged_md_path):
        os.remove(merged_md_path)
