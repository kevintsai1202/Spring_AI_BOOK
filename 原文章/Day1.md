## 使用 Spring AI 打造企業 RAG 知識庫【1】- Spring AI 簡介 (2025最新版)

> **更新說明**：本文基於 Spring AI 1.0.0 GA版本（2025年5月20日發佈）進行更新，包含最新的功能特性和依賴資訊。

# Java在AI看到了春天

![https://ithelp.ithome.com.tw/upload/images/20240810/20161290cAV3EcKpVN.jpg](https://ithelp.ithome.com.tw/upload/images/20240810/20161290cAV3EcKpVN.jpg)

Spring AI 在2024年2月推出了0.8版本，經過一年多的快速迭代，於2025年5月20日正式發佈了**1.0.0 GA穩定版**。從早期的 Milestone 版本（1.0.0-M1到M8）到 RC1 版本，最終迎來了正式的 GA 版本，如同 AI 的發展一樣迅速。實際使用後，會發現它與 **LangChain4j** 有幾分相似。讓我們看看 Spring AI 官網是如何詮釋

> Spring AI 創建的目的在簡化開發 AI 功能的應用程式，同時避免不必要的複雜性。專案汲取了 LangChain 和 LlamaIndex 等知名 Python 專案的靈感，但 Spring AI 並不是這些專案的直接移植。我們相信，下一波生成式人工智慧應用程式不僅適用於 Python 開發人員，還將廣泛適用於許多程式設計語言。
>
> Spring AI 的核心在於解決 AI 整合的根本挑戰，即將 **企業數據** / **API** / **AI** 串聯起來。
>
> ![https://ithelp.ithome.com.tw/upload/images/20240801/20161290yEyOlVWozQ.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290yEyOlVWozQ.png)
>
> (以上由 [Spring AI官網](https://docs.spring.io/spring-ai/reference/index.html) 翻譯而來)

原來 Spring AI 是跟 LangChain 致敬，Spring 框架能發展到這麼龐大，最關鍵的就是其整合以及簡化的能力，面對這麼龐大的模型以及種類，雖然跟 Python 比起來起步較慢，不過能與 Spring 其他框架整合在一起，讓憋了好久的 Java 開發人員也能大展身手了

既然是參考 LangChain 的概念，Java 開發人員該使用 LangChain4j 還是 Spring AI 做為開發框架？凱文大叔幫大家分析以下幾點，大家可以自行評估

## Java 兩大 AI 框架比較 (2025年更新版)

|  | Spring AI | LangChain4j |
| --- | --- | --- |
| 推出時間 | 2024年二月推出0.8版，2025年5月正式發佈1.0.0 GA | 2023年六月推出0.1版 |
| 適用Java版本 | JDK 17+ | JDK 8+ |
| 當前版本 | 1.0.0 GA (穩定版) | 持續更新中，已有80+版本 |
| 功能性 | 整合性更佳，支援20+個AI模型，統一的ChatClient API，支援多模態輸入輸出 | 個別功能較強，部分功能針對不同大語言模型特性開發 |
| 整合性 | 原生支援Spring Boot 3.4.x，完美的自動配置和依賴注入 | 雖然也能在Spring中使用，但整合性不如Spring原生 |
| 核心特色 | ChatClient API、Advisor攔截器鏈、強化版LLM能力 | 豐富的工具生態、活躍的社群支援 |
| 向量資料庫支援 | 支援12+種向量資料庫（Chroma、PGVector、Redis等） | 支援多種向量資料庫 |
| 相關資料 | 官方文檔完整，1.0 GA版本文檔齊全 | 發展時間較長，社群資源豐富 |

## Spring AI 1.0 GA 的重大特色

### ChatClient - 核心API
Spring AI 的核心是 ChatClient，這是一個可攜式且易於使用的 API，是與 AI 模型互動的主要介面。Spring AI 的 ChatClient 支援調用 20 個 AI 模型，從 Anthropic 到智譜AI。它支援多模態輸入和輸出（當底層模型支援時）以及結構化回應。

### 增強版LLM功能
增強版LLM的概念是在基本模型互動的基礎上增加功能，如資料檢索、對話記憶和工具調用。這些功能讓您可以直接將自己的資料和外部API帶入模型的推理過程中。

### Advisor API
Spring AI ChatClient 的一個關鍵特色是 Advisor API。這是一個攔截器鏈，允許您透過注入檢索到的資料和對話記憶來修改傳入的提示詞。

## AI框架挑選建議 (2025年更新)

如何選擇凱文大叔有以下幾點建議

- 如果公司還在使用 Java 8，LangChain4j 是不二選擇
- 如果公司已使用 Spring Boot 3.4+ 以上，當然就直接使用 Spring AI 1.0 GA
- 如果使用 Java 17+ 卻沒使用 Spring Boot 框架呢？建議你趕快學 Spring Boot ![/images/emoticon/emoticon39.gif](https://ithelp.ithome.com.tw/images/emoticon/emoticon39.gif)
- 想嘗試最新功能或是單純開發 AI 的程式可使用 LangChain4j，若要將 AI 結合企業內部其他資訊系統則建議使用 Spring AI，因為開發企業使用的程式往往需要更高的穩定性以及精準的內容
- **2025年重點**：Spring AI 1.0 GA 已經達到生產就緒標準，適合企業級應用開發

下面兩張圖大家可以感受一下兩個 AI 框架設計的理念，Spring AI 就跟 Spring 家族設計理念一樣，以整合為主，而 LangChain4j 則是各模組功能較強

![https://ithelp.ithome.com.tw/upload/images/20240801/20161290sBYJbpR5R4.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290sBYJbpR5R4.png)

[取自稀土掘金](https://juejin.cn/post/7375083022612086818)

![https://ithelp.ithome.com.tw/upload/images/20240801/20161290gqrJyYqnBo.png](https://ithelp.ithome.com.tw/upload/images/20240801/20161290gqrJyYqnBo.png)

[取自LangChain4j](https://docs.langchain4j.dev/intro)

有一點要特別說明 Spring AI 1.0 GA 版本已經非常穩定，而且響應速度很快。例如 7/25 Ollama 才宣布 [支援 Tool](https://ollama.com/blog/tool-support)，Spring 隔一天也宣布 [支援 Ollama Tool](https://spring.io/blog/2024/07/26/spring-ai-with-ollama-tool-support)，可見得 Spring AI 的抽象可以相容不同模型

## Spring AI 1.0 GA 支援的AI模型

Spring AI 1.0 GA 支援以下 AI 模型：
- **OpenAI**：GPT-4o、GPT-4o mini、GPT-3.5 Turbo 等
- **Anthropic**：Claude 3 系列
- **Google**：Vertex AI、Gemini
- **Azure OpenAI**：完整的 Azure 整合
- **AWS Bedrock**：支援多種 Bedrock 模型
- **Ollama**：本地部署模型
- **Groq**：高效能推理
- **其他**：Mistral AI、DeepSeek、QianFan、ZhiPu AI 等

## 鐵人賽大綱

**使用 Spring AI 打造企業 RAG 知識庫** 內容將分為四大章節

- **認識 Spring AI** : 這個章節介紹如何取得 API Key、建立 Spring starter 專案、參數配置、如何與 AI 對話以及讓對話更流暢的流式輸出
- **個性化 ChatBot** : 這個章節會更進一步設定對話的細節，包含系統提示詞、系統人設、提示詞範本、結構化輸出、Function Call
- **讓 ChatBot 不在金魚腦** : ChatGPT 除了理解及生成，還有一個很重要的功能就是記憶，這個章節會先介紹記憶的原理，後面則會介紹 Spring AI 1.0.0 才加入的 ChatMemory
- **讓企業不再卻步的技術-RAG** : 前面幾個章節說穿了只是做一個聊天機器人，很多企業怕資訊外洩甚至禁用 AI，這個章節就來介紹讓 AI 融入企業的法寶 - RAG，透過 Spring Boot 3 以後才支援的 docker compose support 可以快速地建立向量資料庫，讓 Spring AI 開發及測試 RAG 更為方便

介紹就到這邊，明天就要開始實戰演練，這次主題是打造企業 RAG 知識庫，主軸還是跟 RAG 有關的模組，沒提到的模組大家可以上 [Spring AI 官網](https://docs.spring.io/spring-ai/reference/index.html) 查看

