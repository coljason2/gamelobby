# 🎮 遊戲大廳 Game Lobby
簡易的網頁遊戲大廳專案，整合多款小遊戲，提供前端切換介面與後端支援服務。

## 🔧 功能介紹
- 🧩 遊戲集合頁：提供統一入口，可點選進入各款小遊戲
- 📜 遊戲紀錄查詢：部分遊戲支援使用者紀錄查詢（例如樂透下注紀錄）
- 🌐 前後端整合：使用 HTML + JavaScript 作為前端介面，Java 後端提供 API

## 📁 目前整合的遊戲

- 🎯 樂透開獎遊戲（Lotto Game）
- 🧠 長短比較（Emoji 選擇遊戲）
- ♟️ 暗棋單人模式（開發中）

## 📂 專案結構
gamelobby/
├── static/ # 前端 HTML/JS 資料夾
│ ├── index.html # 遊戲大廳首頁
│ ├── lotto.html # 樂透遊戲頁面
│ ├── length.html # 長短比較頁面
│ └── darkchess.html # 暗棋遊戲頁面
├── controller/ # Java 控制器
├── model/ # 遊戲資料模型
├── service/ # 核心邏輯服務
└── GameLobbyApplication.java
