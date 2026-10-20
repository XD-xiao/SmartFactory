# 慧控工厂 Smart Factory

面向工厂的 Web 管理系统，基于 **Spring Boot + Vue 3** 构建，覆盖设备管理、产品管理、人事管理等核心业务。系统通过 **MQTT** 采集传感器实时数据，并结合 **AI（DeepSeek）智能检测** 实现工厂数字化与智能化升级。

> ⚠️ 本仓库已移除所有个人隐私信息与硬编码密钥。部署前请先阅读 [配置说明](#-配置说明)，通过环境变量注入数据库、MQTT、邮件、AI 等敏感配置。

---

## ✨ 功能特性

- **数字大屏**：实时展示传感器数据、生产产量、人员信息与 AI 监控报告（WebSocket 推送）
- **设备管理**：传感器配置与采集、AI 检测记录、PLC 远程控制、展区分组、机器/设备与维修工单管理
- **产品管理**：产品类型 / 型号 / 列表、出入库记录、产量登记与质检，支持条码与二维码生成/识别
- **人事管理**：部门 / 职级 / 员工 / 考勤 / 请假
- **权限控制**：JWT 认证 + 基于角色的路由守卫（`admin` / `device_admin` / `product_admin` / `hr_admin`）
- **AI 智能检测**：基于 LangChain4j + DeepSeek 的传感器数据异常检测与邮件通知

---

## 🛠 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.0.7 | Web 框架 |
| Java | 17 | 运行环境 |
| MyBatis-Plus | 3.5.14 | ORM / 数据库操作 |
| MySQL | 9.5.0 | 数据库 |
| Eclipse Paho + Spring Integration | 1.2.5 | MQTT 客户端（设备通信） |
| WebSocket（STOMP / SockJS） | - | 前端实时推送 |
| JWT（jjwt） | 0.11.5 | 身份认证 |
| Fastjson2 | 2.0.61 | JSON 序列化 |
| LangChain4j + DeepSeek | 1.1.0 | AI 智能体检测 |
| ZXing | 3.5.4 | 条形码 / 二维码识别 |
| Hutool | 5.8.46 | 通用工具库 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue | 3.5 | 前端框架 |
| Vite | 7.0 | 构建工具 |
| Element Plus | 2.11 | UI 组件库 |
| Pinia | 3.0 | 状态管理 |
| Vue Router | 4.5 | 路由管理 |
| ECharts | 6.1 | 数据可视化（大屏看板） |
| MQTT.js | 5.15 | MQTT 客户端（Demo 页） |
| STOMP.js + SockJS | 7.3 | WebSocket 客户端（大屏实时数据） |
| QRCode + JsBarcode | - | 二维码 / 条形码生成 |

---

## 📁 项目结构

```
smart-factory/
├── SmartFactoryService/          # 后端服务 (Spring Boot)
│   ├── src/main/java/com/example/smartfactoryservice/
│   │   ├── Config/               # 配置类 (MQTT、WebSocket、DeepSeek、CORS)
│   │   ├── Controller/           # 控制器层
│   │   │   ├── AgentModule/      #   AI 智能体检测
│   │   │   ├── DeviceModule/     #   设备 / 传感器 / PLC / 分组 / 维修
│   │   │   ├── PersonManagementModule/  # 人事 (部门/职位/员工/考勤/请假)
│   │   │   └── ProductModule/    #   产品 (类型/型号/列表/产量/库存)
│   │   ├── Domain/               # 实体 / MQTT 消息体 / 视图对象 (Result、PageBean)
│   │   ├── Exception/            # 全局异常处理
│   │   ├── Interceptor/          # 登录拦截器 (JWT)
│   │   ├── Mapper/               # MyBatis-Plus Mapper 接口
│   │   ├── Service/              # 业务逻辑层
│   │   ├── Tools/                # Agent 检测工具
│   │   ├── Utils/                # 工具类 (JWT、密码、邮件、校验等)
│   │   ├── gateway/              # MQTT 网关
│   │   └── mqtt/                 # 自定义 MQTT 注解路由 (@MqttController / @MqttSubscribe)
│   ├── src/main/resources/
│   │   └── application.yml       # 主配置文件（敏感项均通过环境变量注入）
│   ├── mvnw / mvnw.cmd           # Maven Wrapper
│   └── pom.xml                   # Maven 依赖
│
├── SmartFactoryWeb/              # 前端管理台 (Vue 3 + Vite)
│   ├── public/
│   │   └── runtime-config.js     # 运行时配置（后端地址，打包后可直接修改）
│   ├── src/
│   │   ├── api/                  # API 接口封装 (axios)
│   │   ├── assets/               # 静态资源
│   │   ├── component/            # 公共组件（大屏/设备/人事/产品）
│   │   ├── config/               # 前端配置（从 runtime-config.js 读取）
│   │   ├── router/               # 路由（含权限守卫）
│   │   ├── stores/               # Pinia 状态管理（token / 角色持久化）
│   │   ├── utils/                # 工具函数（axios 封装、WebSocket、markdown）
│   │   └── views/                # 页面视图（大屏/设备/人事/产品）
│   ├── index.html                # HTML 模板
│   ├── package.json              # 前端依赖
│   └── vite.config.js            # Vite 配置
│
└── Doc/                          # 项目文档（设计、数据库、接口、MQTT 等）
```

---

## 🚀 快速开始

### 前置条件

- **JDK 17+**（后端）
- **Node.js 20.19+ 或 22.12+**（前端）
- **MySQL 8+** — 创建 `smartfactorydb` 数据库并导入 `Doc/数据库设计/script.sql`
- **MQTT Broker** — 如 EMQX / Mosquitto（非必须，调试阶段可关闭）

### 1. 启动后端

1. 配置环境变量（详见 [配置说明](#-配置说明)），至少需要设置 `DB_PASSWORD`（以及可选的 `DB_URL` / `DB_USERNAME`）
2. 若本地没有 MQTT Broker，保持 `MQTT_ENABLED=false`（默认）
3. 进入 `SmartFactoryService` 目录执行：

   **Windows (PowerShell):**
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   **Linux / macOS:**
   ```bash
   ./mvnw spring-boot:run
   ```

4. 服务默认监听 `http://localhost:8080`，健康检查：`http://localhost:8080/actuator/health`

### 2. 启动前端

1. 后端地址默认指向 `http://127.0.0.1:8080`，如需修改：
   - 开发环境：修改 `SmartFactoryWeb/src/config/runtimeConfig.ts` 中的 `DEFAULT_BACKEND_ORIGIN`
   - 生产环境：修改 `SmartFactoryWeb/public/runtime-config.js` 中的 `backendOrigin`
2. 进入 `SmartFactoryWeb` 目录执行：

   ```powershell
   npm install        # 安装依赖
   npm run dev        # 启动开发服务器
   ```

3. 浏览器访问 Vite 输出的地址（默认 `http://localhost:5173`）

### 3. 生产构建

```powershell
npm run build        # 输出到 SmartFactoryWeb/dist/
```

构建产物为纯静态文件，可直接部署到 Nginx 等 Web 服务器。

---

## ⚙️ 配置说明

所有敏感配置均通过**环境变量**注入，仓库内不包含任何明文密钥。下表列出全部环境变量及其默认值：

| 环境变量 | 对应配置 | 默认值 | 说明 |
|----------|----------|--------|------|
| `DB_URL` | `spring.datasource.url` | `jdbc:mysql://localhost:3306/smartfactorydb?...` | MySQL 连接地址 |
| `DB_USERNAME` | `spring.datasource.username` | `root` | 数据库用户名 |
| `DB_PASSWORD` | `spring.datasource.password` | 空 | 数据库密码（**必填**） |
| `MQTT_ENABLED` | `spring.mqtt.enabled` | `false` | MQTT 开关 |
| `MQTT_URL` | `spring.mqtt.url` | `tcp://localhost:1883` | MQTT Broker 地址 |
| `MQTT_USERNAME` | `spring.mqtt.username` | 空 | MQTT 用户名 |
| `MQTT_PASSWORD` | `spring.mqtt.password` | 空 | MQTT 密码 |
| `MAIL_USERNAME` | `spring.mail.username` | 空 | SMTP 发件邮箱 |
| `MAIL_PASSWORD` | `spring.mail.password` | 空 | SMTP 邮箱授权码 |
| `DEEPSEEK_API_KEY` | `deepseek.api-key` | 空 | DeepSeek API Key |
| `ADMIN_EMPLOYEE_NUMBER` | `admin.employee-number` | `adminadmin` | 超级管理员工号 |
| `ADMIN_PASSWORD` | `admin.password` | 空 | 超级管理员密码（**必填**，否则该账号禁用） |
| `JWT_SECRET` | JWT 签名密钥 | 开发默认值 | 生产环境务必覆盖 |

> **部署提示**：生产环境请务必设置 `DB_PASSWORD`、`ADMIN_PASSWORD`、`JWT_SECRET`，以及启用 AI 检测所需的 `DEEPSEEK_API_KEY`。

---

## 📚 相关文档

项目详细设计文档位于 `Doc/` 目录：

- `Doc/文档/# 智能工厂系统 — 完整开发文档.md` — 完整开发文档
- `Doc/数据库设计/` — 数据库设计与建表脚本
- `Doc/请求接口设计/` — 各模块接口设计
- `Doc/MQTT设计/` — PLC 机柜 / 传感器 MQTT 通信设计

---

## 许可证

本仓库暂未声明开源许可证。若需使用或分发，请先与项目作者确认。
