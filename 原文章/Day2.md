## 使用 Spring AI 打造企業 RAG 知識庫【2】- 取得 AI 入門鑰匙 API key (2025最新版)

> **更新說明**：本文基於 Spring AI 1.0.0 GA版本（2025年5月20日發佈）進行更新，包含最新的專案建立方式和依賴配置。

# 開啟AI的鑰匙

![https://ithelp.ithome.com.tw/upload/images/20240810/20161290sQLWvAdoDT.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/20161290sQLWvAdoDT.jpg)

平常使用 ChatGPT 或其他類似的對話 AI 都可直接使用，大不了額度沒了會降為效果比較差的模型，不過要進行程式開發就需要先申請 API key，除非使用本地的 LLM。

## 取得 API KEY

原本凱文大叔測試 AI 都以本地 LLM 或是一些免費的服務為主，不過 2024/7/18 [OpenAI 發布 GPT-4o mini](https://openai.com/index/gpt-4o-mini-advancing-cost-efficient-intelligence/) 模型後就完全改觀了，價格竟然比 GPT-3.5 Turbo 還便宜 60%，花 5 美金就可以用很久(Open AI 最低額度)，平時想省點費用還可以改用尚未收費的 [Groq](https://groq.com/)，這也會讓原本猶豫不決的開發人員一起加入 AI 的賽道，接下來就跟大家說這把鑰匙在哪創建

### Open AI

原本 Open AI 個人的 API key 是在 Profile 下創建，不過前陣子已改為在 Project 下建立

首先進入 [Open AI](https://openai.com/) 後臺管理介面

![https://ithelp.ithome.com.tw/upload/images/20240801/20161290apeeSQnJDZ.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290apeeSQnJDZ.png)

登入後點選 Dashboard→API Keys→Create new secret key 就能建立 API key

![https://ithelp.ithome.com.tw/upload/images/20240801/20161290BT08cfgzNC.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290BT08cfgzNC.png)

名稱可以隨便取，預設會有一個 Default project，若有多個專案也可以建立好 Project 再來建立 API key，有特別權限需求可以在 Permissions 設定，記得產生 API key 後要馬上複製，不然視窗關閉後就無法取得完整的 API key ，只能重新建立一組

![https://ithelp.ithome.com.tw/upload/images/20240801/20161290gsRpyLd5Y0.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290gsRpyLd5Y0.png)

![https://ithelp.ithome.com.tw/upload/images/20240801/20161290f2pGObtkJI.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290f2pGObtkJI.png)

> API key 記得保存好也別洩漏出去，不然額度怎麼被用完都不曉得

### Groq:

Groq 並不是製作 AI 模型的公司，它主要在提供雲端算力，而且上面可選擇一些不錯的開源模型，就連最近剛出的Llama3.1 也能使用，很適合自己電腦跑不動的開發人員，目前都還未開始收費，很適合拿來練習及開發

建立 key 的方式也非常簡單，在 [主頁](https://groq.com/) 點選 Start Building，即可進入後臺

![https://ithelp.ithome.com.tw/upload/images/20240803/20161290LdWP8GjbT2.png](https://ithelp.ithome.com.tw/upload/images/20240803/20161290LdWP8GjbT2.png)

接著點選 API Keys→Create API Key 輸入 Key name 就能產生 API key

![https://ithelp.ithome.com.tw/upload/images/20240801/20161290cu3vSO0CoQ.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290cu3vSO0CoQ.png)![https://ithelp.ithome.com.tw/upload/images/20240801/20161290SIPY5U0tJQ.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290SIPY5U0tJQ.png)

> 與 Open AI 一樣沒複製就查不到了

## 建立 Spring AI 1.0 GA 專案

有了入門鑰匙就能開始創建 Spring 專案，凱文大叔使用的 IDE 是 Spring 官方提供的 [Spring Tools 4 for Eclipse](https://spring.io/tools)，免費又好用，許多功能都直接整合進 IDE

### 使用 Spring Initializr

**2025年更新**：Spring AI 1.0 GA 已經可以直接在 [start.spring.io](https://start.spring.io/) 中選擇，無需額外的配置。

先點選 File → New → Project，接著選擇 Spring Starter Project

![https://ithelp.ithome.com.tw/upload/images/20240801/20161290ZGygob4JoO.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290ZGygob4JoO.png)![https://ithelp.ithome.com.tw/upload/images/20240801/20161290MlXNSovjmu.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290MlXNSovjmu.png)

專案相關名稱填寫完畢就可以勾選需要的依賴，目前 Java Version 只能選到 17 以後的版本，**Spring Boot Version 建議選擇 3.4.x 版本**以獲得最佳的 Spring AI 支援。

![https://ithelp.ithome.com.tw/upload/images/20240801/201612905XYnrzLoKq.png](https://ithelp.ithome.com.tw/upload/images/20240801/201612905XYnrzLoKq.png)![https://ithelp.ithome.com.tw/upload/images/20240801/2016129065EN0h2R4x.png](https://ithelp.ithome.com.tw/upload/images/20240801/2016129065EN0h2R4x.png)

### Spring AI 1.0 GA 依賴選擇

凱文大叔建議勾選的依賴有：

**AI 相關：**
- **OpenAI**: 我們的主角，現在是穩定的 1.0 GA 版本
- **Vector Database** (選擇性)：如 PostgreSQL/PGVector、Chroma 等

**開發工具：**
- **Spring Boot DevTools**: 開發 Web 程式時修改 Class 內容會自動重啟 Tomcat
- **Lombok**: 透過標註簡化 Class 的許多方法，例如 Setter / Getter / Constructor 都能自動生成
- **Spring Web**: 撰寫 API 的必備依賴

### 手動配置 Maven 依賴 (推薦方式)

如果你偏好手動配置，以下是 Spring AI 1.0 GA 的最新 Maven 配置：

```xml
<!-- Spring AI BOM -->
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<!-- Spring AI OpenAI Starter -->
<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-openai-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

## 啟動測試

專案建立完成後先執行看看

![https://ithelp.ithome.com.tw/upload/images/20240801/20161290rMjLePwFJQ.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290rMjLePwFJQ.png)

如果沒有設定 API key，會看到類似的錯誤訊息：

> org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'openAiChatModel' defined in class path resource \[org/springframework/ai/autoconfigure/openai/OpenAiAutoConfiguration.class\]: Failed to instantiate \[org.springframework.ai.openai.OpenAiChatModel\]: Factory method 'openAiChatModel' threw exception with message: OpenAI API key must be set

## Spring AI 1.0 GA 專案參數設定

原來是 Spring Boot 的自動配置找不到 API key，趕快把剛剛創建的 key 設定在 application.yml 中，凱文大叔習慣有結構性的 yml 設定，直接把 src/main/resources 下的 application.properties 附檔名改掉即可，接著按照下面的格式把 API key 填上

### Open AI (2025最新配置)

```yaml
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY:your-api-key-here}
      chat:
        options:
          model: gpt-4o-mini  # 推薦使用最新的 gpt-4o-mini 模型
          temperature: 0.7
          max-tokens: 1000
```

**安全性提醒**：建議使用環境變數來儲存 API key，而不是直接寫在配置檔中。

### Groq (2025最新配置)

```yaml
spring:
  ai:
    openai:
      api-key: ${GROQ_API_KEY:your-groq-api-key-here}
      base-url: https://api.groq.com/openai/v1
      # groq與open ai相容，其他設定都一樣只需增加 base-url 即可
      chat:
        options:
          model: llama-3.1-70b-versatile  # 使用 Groq 提供的最新模型
          temperature: 0.7
```

## Spring AI 1.0 GA 新特色

### ChatClient API
Spring AI 1.0 GA 引入了全新的 ChatClient API，提供更簡潔的使用方式：

```java
@RestController
public class ChatController {
    
    private final ChatClient chatClient;
    
    public ChatController(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }
    
    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }
}
```

### 支援的模型提供商 (2025更新)
Spring AI 1.0 GA 支援20+個AI模型提供商：
- OpenAI (GPT-4o, GPT-4o mini)
- Anthropic (Claude 3 系列)
- Google (Vertex AI, Gemini)
- Azure OpenAI
- AWS Bedrock
- Ollama (本地部署)
- Groq (高效能推理)
- Mistral AI
- DeepSeek
- 智譜AI (ZhiPu)
- 千帆大模型 (QianFan)

再執行一次程式，這次順利啟動沒任何錯誤，表示自動配置沒問題，Spring 容器會自動幫我們建立相關的 Bean

![https://ithelp.ithome.com.tw/upload/images/20240801/201612909K9m7zEAzK.png](https://ithelp.ithome.com.tw/upload/images/20240801/201612909K9m7zEAzK.png)

Open AI 若還沒儲值也能先創建 API key，程式到這都沒問題，不過接下來跟 AI 對話若沒額度就會失敗了，想先體驗 AI 的朋友也可以改用 groq

## 回顧

總結一下今天學到了甚麼?

- 如何取得 API key (OpenAI 和 Groq)
- 在 Spring Tool 4 建立一個 Spring AI 1.0 GA 專案
- 了解 Spring AI 1.0 GA 的新特色和支援的模型
- 將 API key 安全地設定在 application.yml 並成功啟動程式
- 認識 ChatClient API 的使用方式

## Spring AI 1.0 GA 的重要變更

**2025年重大更新**：
1. **穩定的API**：1.0 GA 版本提供穩定的 API，適合生產環境使用
2. **統一的ChatClient**：全新的 ChatClient API 提供一致的使用體驗
3. **增強的自動配置**：更完善的 Spring Boot 自動配置支援
4. **豐富的模型支援**：支援20+個不同的AI模型提供商
5. **企業級功能**：包含 Advisor API、記憶管理、RAG 支援等

> 題外話：沉浸式翻譯可以自訂相容於 OpenAI 的 API，我們可以把 groq 的 API 設上去就能免費使用 AI 翻譯囉

## Source Code

雖然程式沒甚麼內容，還是放在 Github 給大家參考如何設定 Spring AI 1.0 GA

[Spring AI 1.0 GA 範例專案](https://github.com/kevintsai1202/SpringBoot-AI-Day2-GA.git)

