# Chapter 0: 前置知識 (Prerequisite)

> **章節性質**：本章為前言與導讀章節，不包含可執行程式碼，主要介紹 Spring AI 的背景、學習路徑和書籍結構。

---

## 📖 章節內容

### 主題
- Spring AI 介紹與發展歷程
- Java AI 框架比較（Spring AI vs LangChain4j）
- 為什麼要學習 Spring Boot 和 Spring MVC
- 本書學習架構說明
- 完整書籍目錄

### 文件位置
- **完整文檔**: `../../docs/chapter0/前言.md`
- **原始文件**: `../../0.md`

---

## 🎯 學習重點

### Spring AI 核心價值
- **ChatClient API**: 統一的 AI 模型互動介面
- **Advisor API**: 攔截器鏈，用於修改提示詞和注入資料
- **增強版 LLM**: 資料檢索、對話記憶、工具調用

### 技術選型建議
- ✅ **使用 Spring AI 的情境**:
  - 公司使用 Spring Boot 3.4+
  - 需要企業級穩定性
  - 要整合現有 Spring 生態系統

- ✅ **使用 LangChain4j 的情境**:
  - 公司使用 Java 8
  - 需要快速原型開發
  - 追求最新功能

---

## 📚 書籍結構

### 第一部分：Spring Boot 基礎 (第1章)
建立 Spring Boot 開發基礎

### 第二部分：Spring MVC 與 API 開發 (第2章)
掌握 RESTful API 開發技能

### 第三部分：生產級 API 開發實踐 (第3章)
學習企業級 API 開發最佳實踐

### 第四部分：Spring AI 核心 (第4-5章)
- 第4章：Spring AI 入門
- 第5章：個性化 ChatBot

### 第五部分：AI 記憶與 RAG (第6-8章)
- 第6章：讓 ChatBot 不再金魚腦
- 第7章：RAG 實作
- 第8章：Advanced RAG

### 第六部分：MCP 整合 (第9章)
整合 Model Context Protocol

---

## 🔧 技術棧

### 核心框架
- **Java**: 21
- **Spring Boot**: 3.2.x+
- **Spring AI**: 1.0.0 GA+
- **Maven**: 3.9+

### AI 服務
- OpenAI (GPT-4/GPT-5)
- Google Gemini (2.5 pro / 2.5 flash)
- Groq (Whisper 語音轉文字)

### 向量資料庫
- Chroma
- PGVector
- Neo4j
- Redis

---

## 📝 備註

本章不包含程式碼範例，為純文檔章節。實際的程式碼範例從第1章開始。

---

**章節狀態**: ✅ 已完成（無程式碼需提取）
**最後更新**: 2025-10-23
