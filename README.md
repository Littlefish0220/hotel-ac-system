# 酒店空调管理系统
## 项目简介
分布式酒店空调管理系统，包含前端Vue界面和后端Spring Boot服务。
## 项目结构
hotel-ac-system/
├── backend/              # 后端Java项目（Spring Boot）
├── frontend/             # 前端Vue项目
└── README.md

<TEXT>
## 技术栈
### 后端
- Java 8+
- Spring Boot
- Maven
### 前端
- Vue 3
- Element Plus
- Axios
## 快速开始
### 后端启动
```bash
cd backend
mvn spring-boot:run
后端服务运行在 http://localhost:8080

前端启动
<BASH>
cd frontend
npm install
npm run dev
前端服务运行在 http://localhost:5173

功能特性
空调远程控制
实时温度监控
智能调度算法
费用计算与账单生成
作者
[你的名字]

许可证
MIT

<TEXT>
---
### 步骤5：在GitHub创建仓库
1. 登录 [GitHub](https://github.com)
2. 点击右上角 `+` → `New repository`
3. 填写信息：
   - **Repository name**: `hotel-ac-system`
   - **Description**: `分布式酒店空调管理系统`
   - **Public/Private**: 选择公开或私有
   - **不要勾选** "Initialize this repository with a README"
4. 点击 `Create repository`
---
### 步骤6：关联远程仓库并推送
```bash
# 在项目根目录执行
# 添加所有文件到暂存区
git add .
# 提交
git commit -m "Initial commit: 前后端项目初始化"
# 关联远程仓库（替换为你的GitHub用户名）
git remote add origin https://github.com/你的用户名/hotel-ac-system.git
# 推送到GitHub
git push -u origin main
# 如果提示分支名是master，改用：
git branch -M main
git push -u origin main
