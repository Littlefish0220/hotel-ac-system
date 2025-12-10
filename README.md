# 完整的 README.md

```markdown
# 🏨 分布式酒店空调管理系统

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Java](https://img.shields.io/badge/Java-8+-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7+-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.0+-brightgreen.svg)](https://vuejs.org/)
[![Element Plus](https://img.shields.io/badge/Element%20Plus-2.0+-409EFF.svg)](https://element-plus.org/)

> 一个基于Spring Boot + Vue 3的分布式酒店空调智能管理系统，实现空调远程控制、温度监控、智能调度和费用计算等功能。

---

## 📋 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [系统架构](#系统架构)
- [核心功能](#核心功能)
- [API文档](#api文档)
- [测试用例](#测试用例)
- [部署指南](#部署指南)
- [常见问题](#常见问题)
- [贡献指南](#贡献指南)
- [许可证](#许可证)
- [联系方式](#联系方式)

---

## 🎯 项目简介

本项目是一个完整的分布式酒店空调管理系统，模拟真实酒店场景中的空调控制与管理需求。系统采用前后端分离架构，实现了：

- 🌡️ **实时温度监控**：监控每个房间的当前温度和目标温度
- 🎛️ **远程控制**：支持开关机、调温、调速等操作
- 🧠 **智能调度**：基于优先级的空调调度算法，最多支持3台同时运行
- 💰 **费用计算**：实时计算空调使用费用，支持详单查询
- 📊 **数据可视化**：直观展示系统运行状态和统计数据

---

## ✨ 功能特性

### 核心功能

- ✅ **空调控制**
  - 开机/关机
  - 温度调节（18-28℃）
  - 风速切换（高/中/低）
  - 模式选择（制冷/制热）

- ✅ **智能调度**
  - 最多3台空调同时运行
  - 基于风速优先级的调度算法
  - 服务时长≥2分钟的低优先级可被抢占
  - 自动待机与回温检测

- ✅ **费用管理**
  - 实时费用计算（1元/℃）
  - 风速差异化计费（高速1℃/min，中速0.5℃/min，低速1/3℃/min）
  - 详单记录与查询
  - 账单生成与打印

- ✅ **系统监控**
  - 实时状态监控
  - 服务队列/等待队列统计
  - 入住天数自动计算
  - 温度变化曲线

### 高级特性

- 🔄 **自动化测试**：内置验收测试用例，一键运行
- 📈 **数据统计**：房间使用率、费用统计、服务时长分析
- 🎨 **美观界面**：毛玻璃效果、渐变背景、动画交互
- 🌐 **跨平台**：支持Windows、Linux、macOS

---

## 🛠️ 技术栈

### 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 8+ | 编程语言 |
| Spring Boot | 2.7+ | 应用框架 |
| Maven | 3.6+ | 项目管理 |
| Lombok | 1.18+ | 简化代码 |

### 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.3+ | 前端框架 |
| Element Plus | 2.4+ | UI组件库 |
| Axios | 1.5+ | HTTP客户端 |
| Vite | 4.5+ | 构建工具 |

### 开发工具

- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **API测试**: Postman / Apifox
- **版本控制**: Git
- **包管理**: Maven / npm

---

## 📁 项目结构

```
hotel-ac-system/
├── backend/                          # 后端Java项目
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       ├── api/          # REST API控制器
│   │       │       │   └── HotelApiController.java
│   │       │       ├── config/       # 系统配置
│   │       │       │   └── SystemContext.java
│   │       │       ├── controller/   # 业务控制器
│   │       │       │   ├── AcController.java
│   │       │       │   ├── BillController.java
│   │       │       │   └── CustomerController.java
│   │       │       ├── model/        # 数据模型
│   │       │       │   └── entity/
│   │       │       │       ├── Room.java
│   │       │       │       ├── AcBill.java
│   │       │       │       ├── AcDetailRecord.java
│   │       │       │       └── ...
│   │       │       ├── repository/   # 数据访问层
│   │       │       │   ├── RoomRepository.java
│   │       │       │   ├── InMemoryRoomRepository.java
│   │       │       │   └── ...
│   │       │       ├── scheduler/    # 调度器
│   │       │       │   ├── Scheduler.java
│   │       │       │   ├── DefaultScheduler.java
│   │       │       │   └── ServiceContext.java
│   │       │       ├── service/      # 业务服务层
│   │       │       │   ├── AcService.java
│   │       │       │   ├── AcBillingService.java
│   │       │       │   └── impl/
│   │       │       │       ├── AcServiceImpl.java
│   │       │       │       ├── AcBillingServiceImpl.java
│   │       │       │       └── ...
│   │       │       └── simulation/   # 温度模拟
│   │       │           └── TemperatureModel.java
│   │       └── resources/
│   │           └── application.properties
│   ├── pom.xml                       # Maven配置
│   └── README.md                     # 后端说明文档
│
├── frontend/                         # 前端Vue项目
│   ├── src/
│   │   ├── api/                      # API接口
│   │   │   ├── index.js
│   │   │   └── testCases.js          # 测试用例
│   │   ├── components/               # Vue组件
│   │   │   └── AcMonitor.vue         # 监控界面
│   │   ├── router/                   # 路由配置
│   │   │   └── index.js
│   │   ├── views/                    # 页面视图
│   │   │   ├── Home.vue
│   │   │   └── Monitor.vue
│   │   ├── App.vue                   # 根组件
│   │   └── main.js                   # 入口文件
│   ├── public/                       # 静态资源
│   ├── package.json                  # npm配置
│   ├── vite.config.js                # Vite配置
│   └── README.md                     # 前端说明文档
│
├── .gitignore                        # Git忽略文件
├── LICENSE                           # 许可证
└── README.md                         # 项目总说明（本文件）
```

---

## 🚀 快速开始

### 环境要求

- **Java**: JDK 8 或更高版本
- **Maven**: 3.6 或更高版本
- **Node.js**: 16.0 或更高版本
- **npm**: 8.0 或更高版本

### 1. 克隆项目

```bash
git clone https://github.com/你的用户名/hotel-ac-system.git
cd hotel-ac-system
```

### 2. 启动后端

```bash
cd backend

# 使用Maven编译
mvn clean install

# 启动Spring Boot应用
mvn spring-boot:run

# 或者直接运行jar包
java -jar target/hotel-ac-system-1.0.0.jar
```

后端服务将运行在 `http://localhost:8080`

**验证后端启动成功**：
```bash
curl http://localhost:8080/api/status
```

### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将运行在 `http://localhost:5173`

**访问系统**：
打开浏览器访问 `http://localhost:5173`

---

## 🏗️ 系统架构

### 整体架构图

```
┌─────────────────────────────────────────────────────────┐
│                      前端 (Vue 3)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │  监控界面    │  │  控制面板    │  │  账单管理    │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
                            │
                    HTTP REST API
                            │
┌─────────────────────────────────────────────────────────┐
│                   后端 (Spring Boot)                     │
│  ┌──────────────────────────────────────────────────┐  │
│  │            HotelApiController                     │  │
│  │         (REST API 统一入口)                       │  │
│  └──────────────────────────────────────────────────┘  │
│                            │                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │ AcController │  │BillController│  │CustomerCtrl  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
│                            │                             │
│  ┌──────────────────────────────────────────────────┐  │
│  │              Service Layer                        │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐       │  │
│  │  │AcService │  │ Billing  │  │Scheduler │       │  │
│  │  └──────────┘  └──────────┘  └──────────┘       │  │
│  └──────────────────────────────────────────────────┘  │
│                            │                             │
│  ┌──────────────────────────────────────────────────┐  │
│  │           Repository Layer                        │  │
│  │  ┌──────────┐  ┌──────────┐  ┌──────────┐       │  │
│  │  │  Room    │  │  Bill    │  │ Customer │       │  │
│  │  └──────────┘  └──────────┘  └──────────┘       │  │
│  └──────────────────────────────────────────────────┘  │
│                            │                             │
│  ┌──────────────────────────────────────────────────┐  │
│  │          In-Memory Database                       │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

### 核心模块说明

#### 1. 调度器 (Scheduler)

**职责**：管理空调的运行状态和调度策略

**核心算法**：
```java
// 优先级计算
priority = fanSpeed * 100 - (runTime >= 2min ? 50 : 0)

// 调度规则
if (服务队列未满) {
    直接进入服务队列
} else if (等待队列中有更高优先级) {
    抢占最低优先级的运行中空调
}
```

**状态转换**：
```
OFF → WAITING → RUNNING → STANDBY → WAITING → ...
```

#### 2. 计费服务 (BillingService)

**计费规则**：
- 基础费率：1元/℃
- 风速系数：
  - 高速：1.0℃/min → 1.0元/min
  - 中速：0.5℃/min → 0.5元/min
  - 低速：1/3℃/min → 0.33元/min

**费用计算公式**：
```
本次费用 = (当前时间 - 服务开始时间) / 60 * 风速费率
累计费用 = 已结算费用 + 本次费用
```

#### 3. 温度模拟 (TemperatureModel)

**温度变化规则**：
- 运行状态：按风速降温/升温
- 等待/待机：以0.5℃/min回温至初始温度
- 关机状态：自然回温

---

## 💡 核心功能

### 1. 空调控制

#### 开机
```http
POST /api/room/control
Content-Type: application/json

{
  "roomNo": "101",
  "action": "powerOn"
}
```

#### 调温
```http
POST /api/room/control
Content-Type: application/json

{
  "roomNo": "101",
  "action": "changeTemp",
  "targetTemp": 22.0
}
```

#### 调速
```http
POST /api/room/control
Content-Type: application/json

{
  "roomNo": "101",
  "action": "changeSpeed",
  "fanSpeed": "HIGH"
}
```

### 2. 系统监控

#### 获取系统状态
```http
GET /api/status
```

**响应示例**：
```json
{
  "rooms": [
    {
      "roomNo": "101",
      "currentTemp": 26.5,
      "targetTemp": 25.0,
      "fanSpeed": "MEDIUM",
      "state": "running",
      "checkInDays": 1,
      "sessionFee": 1.5,
      "fee": 5.0
    }
  ],
  "system": {
    "timeCounter": 15,
    "isSystemOn": true,
    "mode": "cool"
  }
}
```

### 3. 时间推进

```http
POST /api/time/tick
```

每次调用推进1分钟，触发：
- 温度更新
- 状态转换
- 费用计算
- 调度重排

---

## 📊 测试用例

### 内置验收测试

系统内置完整的验收测试用例，覆盖以下场景：

1. **基础功能测试**
   - 开关机
   - 调温调速
   - 温度变化

2. **调度算法测试**
   - 优先级调度
   - 抢占机制
   - 队列管理

3. **计费功能测试**
   - 实时计费
   - 风速切换计费
   - 账单生成

### 运行测试

**前端界面运行**：
1. 访问监控界面
2. 点击"启动验收测试"按钮
3. 系统自动执行测试用例
4. 观察日志和状态变化

**测试用例配置**（`frontend/src/api/testCases.js`）：
```javascript
export const TEST_SCENARIO = [
  { timeOffset: 0, roomNo: '101', action: 'powerOn' },
  { timeOffset: 0, roomNo: '102', action: 'powerOn' },
  { timeOffset: 5, roomNo: '101', action: 'changeTemp', targetTemp: 22 },
  // ... 更多测试步骤
]
```

---

## 🌐 API文档

### 基础信息

- **Base URL**: `http://localhost:8080/api`
- **Content-Type**: `application/json`
- **响应格式**: JSON

### 接口列表

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 获取系统状态 | GET | `/status` | 获取所有房间状态 |
| 控制房间 | POST | `/room/control` | 开关机/调温/调速 |
| 时间推进 | POST | `/time/tick` | 推进1分钟 |
| 入住 | POST | `/checkIn` | 办理入住 |
| 退房 | POST | `/checkOut` | 办理退房 |
| 账单预览 | GET | `/bill/preview/{roomNo}` | 查看账单 |
| 详单查询 | GET | `/bill/details/{roomNo}` | 查看详单 |
| 系统重置 | POST | `/reset` | 重置系统 |

### 详细接口文档

#### 1. 获取系统状态

**请求**：
```http
GET /api/status
```

**响应**：
```json
{
  "rooms": [
    {
      "roomNo": "101",
      "initialTemp": 32.0,
      "currentTemp": 26.5,
      "targetTemp": 25.0,
      "fanSpeed": "MEDIUM",
      "state": "running",
      "checkInDays": 1,
      "sessionFee": 1.5,
      "acFee": 3.5,
      "fee": 5.0,
      "totalRoomFee": 100.0
    }
  ],
  "system": {
    "timeCounter": 15,
    "isSystemOn": true,
    "mode": "cool",
    "maxLimit": 3
  }
}
```

#### 2. 控制房间

**请求**：
```http
POST /api/room/control
Content-Type: application/json

{
  "roomNo": "101",
  "action": "powerOn",           // powerOn | powerOff
  "targetTemp": 22.0,             // 可选，18-28
  "fanSpeed": "HIGH"              // 可选，HIGH | MEDIUM | LOW
}
```

**响应**：
```json
{
  "code": 200,
  "msg": "操作成功"
}
```

---

## 🚢 部署指南

### Docker部署（推荐）

#### 1. 创建Dockerfile（后端）

```dockerfile
# backend/Dockerfile
FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
```

#### 2. 创建Dockerfile（前端）

```dockerfile
# frontend/Dockerfile
FROM node:16-alpine as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
```

#### 3. 创建docker-compose.yml

```yaml
version: '3.8'

services:
  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
    networks:
      - hotel-network

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - hotel-network

networks:
  hotel-network:
    driver: bridge
```

#### 4. 启动服务

```bash
docker-compose up -d
```

### 传统部署

#### 后端部署

```bash
# 1. 打包
cd backend
mvn clean package -DskipTests

# 2. 运行
java -jar target/hotel-ac-system-1.0.0.jar

# 3. 后台运行
nohup java -jar target/hotel-ac-system-1.0.0.jar > app.log 2>&1 &
```

#### 前端部署

```bash
# 1. 构建
cd frontend
npm run build

# 2. 部署到Nginx
cp -r dist/* /usr/share/nginx/html/

# 3. 配置Nginx反向代理
# /etc/nginx/conf.d/hotel.conf
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

---

## ❓ 常见问题

### Q1: 后端启动失败，提示端口被占用

**A**: 修改端口配置

```properties
# backend/src/main/resources/application.properties
server.port=8081
```

### Q2: 前端无法连接后端

**A**: 检查API地址配置

```javascript
// frontend/src/api/index.js
const BASE_URL = 'http://localhost:8080/api'
```

### Q3: 入住天数显示为0

**A**: 确保已修改以下文件：
- `Room.java` - 添加 `checkInDays` 字段
- `DefaultScheduler.java` - 开机时调用 `incrementCheckInDays()`
- `HotelApiController.java` - 返回 `room.getCheckInDays()`

### Q4: 费用计算不准确

**A**: 检查计费服务是否正确实现：
- `AcBillingServiceImpl.getCurrentSessionFee()` - 实时费用
- `AcBillingServiceImpl.getRoomTotalFee()` - 累计费用

### Q5: 测试用例无法运行

**A**: 确保：
1. 后端服务已启动
2. 前端已正确配置API地址
3. 浏览器控制台无错误

---

## 🤝 贡献指南

欢迎贡献代码！请遵循以下步骤：

### 1. Fork项目

点击右上角的 `Fork` 按钮

### 2. 创建分支

```bash
git checkout -b feature/新功能名称
```

### 3. 提交代码

```bash
git add .
git commit -m "feat: 添加新功能"
```

**提交信息规范**：
- `feat`: 新功能
- `fix`: 修复bug
- `docs`: 文档更新
- `style`: 代码格式调整
- `refactor`: 重构
- `test`: 测试相关
- `chore`: 构建/工具相关

### 4. 推送分支

```bash
git push origin feature/新功能名称
```

### 5. 创建Pull Request

在GitHub上创建PR，描述你的修改

---

## 📄 许可证

本项目采用 [MIT License](LICENSE) 开源协议。

```
MIT License

Copyright (c) 2024 [你的名字]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 📧 联系方式

- **作者**: [你的名字]
- **邮箱**: your.email@example.com
- **GitHub**: [@你的用户名](https://github.com/你的用户名)
- **项目主页**: [https://github.com/你的用户名/hotel-ac-system](https://github.com/你的用户名/hotel-ac-system)

---

## 🙏 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Maven](https://maven.apache.org/)
- [Vite](https://vitejs.dev/)

---

## 📈 项目统计

![GitHub stars](https://img.shields.io/github/stars/你的用户名/hotel-ac-system?style=social)
![GitHub forks](https://img.shields.io/github/forks/你的用户名/hotel-ac-system?style=social)
![GitHub issues](https://img.shields.io/github/issues/你的用户名/hotel-ac-system)
![GitHub pull requests](https://img.shields.io/github/issues-pr/你的用户名/hotel-ac-system)

---

## 🗺️ 路线图

- [x] 基础功能实现
- [x] 智能调度算法
- [x] 费用计算系统
- [x] 前端监控界面
- [ ] 数据持久化（MySQL）
- [ ] 用户权限管理
- [ ] 移动端适配
- [ ] 数据报表导出
- [ ] 微服务架构改造

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个Star！⭐**

Made with ❤️ by [你的名字]

</div>
```

---

## 使用说明

1. **替换占位符**：
   - `你的用户名` → 你的GitHub用户名
   - `你的名字` → 你的真实姓名或昵称
   - `your.email@example.com` → 你的邮箱

2. **添加徽章**：
   - 访问 [shields.io](https://shields.io/) 生成更多徽章
   - 替换项目链接

3. **自定义内容**：
   - 根据实际功能调整"功能特性"部分
   - 补充实际的API文档
   - 添加截图和演示视频

4. **保存文件**：
   - 将内容保存为 `README.md`
   - 放在项目根目录

这个README包含了项目的完整信息，专业且易读，适合开源项目展示！
