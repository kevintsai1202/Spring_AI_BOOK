# 前端流式輸出範例

>展示如何在前端接收和處理 Spring AI 的流式輸出（Server-Sent Events）

---

## 📂 檔案說明

| 檔案 | 說明 | 技術 | 難度 |
|------|------|------|------|
| **index.html** | EventSource 基礎範例 | 純 HTML/JavaScript | ⭐️ 入門 |
| **streaming-demo.html** | 完整流式聊天介面 | 純 HTML/JavaScript | ⭐️⭐️ 進階 |
| **react-example.html** | React Hooks 整合範例 | React (CDN) | ⭐️⭐️⭐️ 進階 |
| **vue-example.html** | Vue 3 Composition API 範例 | Vue 3 (CDN) | ⭐️⭐️⭐️ 進階 |

---

## 🚀 快速開始

### 前置需求

1. **後端服務已啟動**
   ```bash
   cd ../
   mvn spring-boot:run
   ```

2. **確認 API 可存取**
   ```bash
   curl "http://localhost:8080/api/ai/chat?prompt=Hello"
   ```

### 使用方式

直接使用瀏覽器開啟 HTML 檔案即可：

```bash
# 方法 1：直接雙擊檔案

# 方法 2：使用瀏覽器開啟
start index.html              # Windows
open index.html               # macOS
xdg-open index.html          # Linux

# 方法 3：使用本地伺服器（推薦）
python -m http.server 3000   # Python 3
# 然後訪問 http://localhost:3000
```

---

## 📖 範例詳解

### 1. index.html - EventSource 基礎範例

**學習重點**：
- ✅ EventSource API 基本用法
- ✅ 監聽 SSE 事件（onopen, onmessage, onerror）
- ✅ 連線狀態管理
- ✅ 錯誤處理

**核心程式碼**：
```javascript
// 建立 EventSource 連線
const eventSource = new EventSource(url);

// 監聽訊息
eventSource.onmessage = function(event) {
    console.log('收到資料:', event.data);
    // 處理接收到的資料
};

// 關閉連線
eventSource.close();
```

**適用場景**：
- 學習 SSE 基礎概念
- 快速測試 API 端點
- 理解流式輸出原理

---

### 2. streaming-demo.html - 完整聊天介面

**學習重點**：
- ✅ 類似 ChatGPT 的使用體驗
- ✅ 即時顯示 AI 回應
- ✅ 對話歷史管理
- ✅ 自動捲動和使用者互動

**功能特色**：
- 🎨 美觀的聊天介面設計
- 💬 支援多輪對話
- 📜 自動/手動捲動切換
- 💾 對話記錄匯出
- ⌨️ 快捷鍵支援（Ctrl+Enter）
- 🎯 範例提示詞快速選擇

**適用場景**：
- 建立生產環境的聊天介面
- 參考 UI/UX 設計
- 完整的功能實作範例

---

### 3. react-example.html - React 整合

**學習重點**：
- ✅ 使用 React Hooks 管理 EventSource
- ✅ useEffect 處理副作用
- ✅ useState 管理狀態
- ✅ 元件化設計

**核心 Hook**：
```javascript
function useEventSource(url) {
    const [data, setData] = useState('');
    const [isConnected, setIsConnected] = useState(false);

    useEffect(() => {
        if (!url) return;

        const eventSource = new EventSource(url);

        eventSource.onmessage = (event) => {
            setData(prev => prev + event.data);
        };

        return () => eventSource.close();
    }, [url]);

    return { data, isConnected };
}
```

**適用場景**：
- React 專案整合
- 學習 Hooks 實作模式
- 元件化開發

---

### 4. vue-example.html - Vue 3 整合

**學習重點**：
- ✅ 使用 Composition API
- ✅ ref 和 reactive 狀態管理
- ✅ onMounted/onUnmounted 生命週期
- ✅ 響應式資料綁定

**核心 Composable**：
```javascript
function useEventSource(url) {
    const data = ref('');
    const isConnected = ref(false);
    let eventSource = null;

    const connect = () => {
        eventSource = new EventSource(url.value);

        eventSource.onmessage = (event) => {
            data.value += event.data;
        };
    };

    onUnmounted(() => {
        eventSource?.close();
    });

    return { data, isConnected, connect };
}
```

**適用場景**：
- Vue 3 專案整合
- 學習 Composition API
- 響應式資料處理

---

## 🔧 配置說明

### API 端點配置

所有範例預設使用以下 API 端點：

```javascript
const API_BASE_URL = 'http://localhost:8080/api/ai/chat/stream';
```

如需修改，請在 HTML 檔案中搜尋 `API_BASE_URL` 或 `apiUrl` 並更新。

### CORS 設定

如果前端和後端在不同的 domain，需要在後端配置 CORS：

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }
}
```

---

## 🐛 常見問題

### Q1: 顯示「連線失敗」錯誤

**解決方法**：
1. 確認後端服務已啟動在 `http://localhost:8080`
2. 檢查 API 端點是否正確
3. 查看瀏覽器 Console 的錯誤訊息
4. 確認防火牆沒有封鎖連線

### Q2: 收不到流式資料

**解決方法**：
1. 確認請求 Header 包含 `Accept: text/event-stream`
2. 檢查後端是否正確返回 SSE 格式
3. 查看 Network 分頁的 EventStream 請求

### Q3: 瀏覽器不支援 EventSource

**解決方法**：
- 使用現代瀏覽器（Chrome、Firefox、Edge、Safari）
- IE 不支援 EventSource，請使用 polyfill

### Q4: 流式輸出中斷

**解決方法**：
1. 檢查網路連線穩定性
2. 增加伺服器的超時時間
3. 實作重連機制

---

## 💡 最佳實踐

### 1. 錯誤處理

```javascript
eventSource.onerror = function(event) {
    console.error('連線錯誤:', event);

    // 自動重連（可選）
    if (eventSource.readyState === EventSource.CLOSED) {
        setTimeout(() => {
            // 重新建立連線
        }, 3000);
    }
};
```

### 2. 記憶體管理

```javascript
// 確保關閉不再使用的連線
window.addEventListener('beforeunload', () => {
    eventSource?.close();
});
```

### 3. 效能優化

```javascript
// 使用 requestAnimationFrame 優化 DOM 更新
let pendingData = '';
let rafId = null;

eventSource.onmessage = (event) => {
    pendingData += event.data;

    if (!rafId) {
        rafId = requestAnimationFrame(() => {
            updateUI(pendingData);
            pendingData = '';
            rafId = null;
        });
    }
};
```

---

## 📚 延伸閱讀

- [MDN: Server-Sent Events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events)
- [MDN: EventSource API](https://developer.mozilla.org/en-US/docs/Web/API/EventSource)
- [Spring AI 官方文件](https://docs.spring.io/spring-ai/reference/)

---

## 🤝 貢獻

歡迎提出改進建議或回報問題！

---

**最後更新**：2025-10-24
**版本**：1.0.0
