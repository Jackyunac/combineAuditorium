# Combine Auditorium

一个包含前后端的视频与直播示例平台，支持视频上传、转码、点播、直播弹幕/礼物、论坛交流与 AI 助手配置。后端基于 Spring Boot 3 + MyBatis-Plus + Redis + MySQL + MinIO + WebSocket + Spring Cloud OpenFeign，前端使用 Vue 3 + TypeScript + Vite + Element Plus。AI 微服务采用 LangChain4j + Google GenAI，当前默认使用 MySQL 存储。

## 目录结构
- `backend/`：主业务后端服务（控制器、服务、mapper、`application.yml` 等）。
- `ai-service/`：AI 配置与 RAG 微服务，独立数据源与迁移脚本在 `src/main/resources/db/migration`。
- `frontend/`：Vue 前端，路由、视图与 API 客户端位于 `src`。
- `docker/`：本地依赖的配置与数据目录（MySQL/Redis/MinIO/SRS）；`docker-compose.yml` 负责启动。
- `AGENTS.md`：贡献者指南；`PROJECT_PLAN_AND_DESIGN.md`：规划记录。

## 环境依赖
- Java 17 与 Maven；Node.js 18+ 与 npm。
- 可选：Docker & Docker Compose（启动 MySQL、Redis、MinIO、SRS）。
- FFmpeg 需在 PATH 下可用，用于视频转码。

## 快速启动（本地）
1) 基础依赖（可选）：`docker-compose up -d mysql redis minio srs`
2) 主后端：`cd backend && mvn spring-boot:run`（默认 8081，使用 `combine_auditorium` 库）
3) AI 微服务：`cd ai-service && mvn spring-boot:run`（默认 8082，使用 `ai_service` 库；可通过环境变量覆盖数据源）
4) 前端：`cd frontend && npm install && npm run dev -- --host`

## 关键配置（均可用环境变量覆盖）
- 数据库：`SPRING_DATASOURCE_URL` / `USERNAME` / `PASSWORD`
- Redis：`SPRING_DATA_REDIS_HOST` / `PORT`
- MinIO：`MINIO_ENDPOINT` / `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY`
- AI：`GOOGLE_AI_API_KEY`、`AI_MODEL`、`AI_TEMPERATURE`、`AI_TOP_K`；知识库向量存储可切换 pgvector（默认为 MySQL）。

## 说明
- 运行时数据（如 `docker/*/data`、`target/`、`node_modules/`）已加入 `.gitignore`，无需纳入版本控制。
- Knife4j 文档默认可在后端服务端口的 `/doc.html` 访问。
- AI 对话默认走配置好的大模型与知识库，无需额外前端配置页面（系统页面仅供 system 角色使用）。
