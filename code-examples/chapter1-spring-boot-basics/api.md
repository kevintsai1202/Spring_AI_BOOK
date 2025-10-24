# Chapter 1 - Spring Boot Basics API 文件

## 版本資訊
- **Base URL**：`http://localhost:8080/api/users`
- **授權**：無（示範專案）
- **內容格式**：`application/json`

## 通用回應格式
| 欄位 | 型別 | 說明 |
| --- | --- | --- |
| `timestamp` | string (ISO-8601) | 發生時間（錯誤時） |
| `status` | integer | HTTP 狀態碼 |
| `error` | string | 錯誤描述 |
| `message` | string | 具體錯誤訊息 |
| `path` | string | 請求路徑 |

> 正常回應為純資料物件或陣列；錯誤回應會符合上述欄位結構。

---

## 1. 取得全部用戶
- **Method / Path**：`GET /api/users`
- **成功回應 200**

```jsonc
[
  {
    "id": 1,
    "name": "小明",
    "email": "ming@example.com"
  }
]
```

---

## 2. 依 ID 取得用戶
- **Method / Path**：`GET /api/users/{id}`
- **路徑參數**
  | 名稱 | 型別 | 必填 | 說明 |
  | --- | --- | --- | --- |
  | `id` | long | 是 | 用戶流水號 |
- **成功回應 200**

```jsonc
{
  "id": 1,
  "name": "小明",
  "email": "ming@example.com"
}
```

- **失敗回應 404**

```jsonc
{
  "timestamp": "2024-10-24T08:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "User not found",
  "path": "/api/users/99"
}
```

---

## 3. 建立用戶
- **Method / Path**：`POST /api/users`
- **Request Body**

```jsonc
{
  "name": "王小華",
  "email": "hua@example.com",
  "password": "secret123"
}
```

- **欄位驗證**
  | 欄位 | 約束 | 訊息 |
  | --- | --- | --- |
  | `name` | `@NotBlank`, `@Size(2,50)` | 名稱必填，長度介於 2~50 |
  | `email` | `@NotBlank`, `@Email` | Email 必填且須符合格式 |
  | `password` | `@NotBlank`, `@Size(6,20)` | 密碼必填，長度介於 6~20 |

- **成功回應 201**

```jsonc
{
  "id": 3,
  "name": "王小華",
  "email": "hua@example.com"
}
```

- **失敗回應 400**

```jsonc
{
  "timestamp": "2024-10-24T08:05:00",
  "status": 400,
  "error": "Bad Request",
  "message": "name: 名稱必填",
  "path": "/api/users"
}
```

---

## 4. 更新用戶
- **Method / Path**：`PUT /api/users/{id}`
- **路徑參數**
  | 名稱 | 型別 | 必填 | 說明 |
  | --- | --- | --- | --- |
  | `id` | long | 是 | 欲更新的用戶 ID |

- **Request Body**：同建立用戶

- **成功回應 200**

```jsonc
{
  "id": 2,
  "name": "更新後姓名",
  "email": "updated@example.com"
}
```

- **失敗回應**
  - `400 Bad Request`：驗證失敗
  - `404 Not Found`：用戶不存在

---

## 5. 刪除用戶
- **Method / Path**：`DELETE /api/users/{id}`
- **成功回應 204**
  - 無內容。
- **容錯**
  - 若 ID 不存在仍回傳 204（示範用途），可依需求調整為 404。

---

## 6. 範例 PowerShell 呼叫
```powershell
# 取得全部用戶
Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/users"

# 建立用戶
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/users" `
    -ContentType "application/json" `
    -Body (@{
        name = "王曉明"
        email = "xm@example.com"
        password = "pass1234"
    } | ConvertTo-Json)
```

---

## 7. 錯誤碼摘要
| 狀態碼 | 說明 | 常見情境 |
| --- | --- | --- |
| 200 | 成功回應 | 查詢或更新成功 |
| 201 | 建立成功 | 新增用戶 |
| 204 | 成功但無內容 | 刪除 |
| 400 | 請求驗證失敗 | 欄位空白或格式不符 |
| 404 | 找不到資源 | 查詢或更新時 ID 不存在 |
