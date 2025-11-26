

一个包含前后端的音视频平台示例，支持视频上传、转码、点播、直播互动与基础用户体系。

技术栈：
- Backend: Java 17、Spring Boot 3、MyBatis-Plus、Redis、MySQL、MinIO、WebSocket、Knife4j、FFmpeg 工具链
- Frontend: Vue 3、TypeScript、Vite、Pinia、Vue Router、Element Plus
- 大模型相关： 使用google官方sdk，通过反向代理链接gemini3提供对话功能，并进行了基础的提示词工程，如你想使用此模块，需要恢复baseUrl默认值并配置你的apiKey

目录结构：
- `backend/` 后端服务源代码
- `frontend/` 前端应用
- `docker/` 
- `docker-compose.yml` 

注意事项：
-后端部署环境需要安装FFmpeg并且配置环境变量

其他：
  该项目开发的目的主要是用于个人学习以及比较各大模型code能力
  前端代码为cursor中使用gemini3.0辅助开发
  后端代码为cursor+claude sonnet 4.5构建基础框架，后采用gpt5.1-codex-max模型，vibe coding的开发模式。
