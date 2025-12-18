# HealthTrack Personal Wellness Platform (Assignment 3 - Phase 3)

本项目实现了课程作业 **HealthTrack Personal Wellness Platform** 的 Phase 3 要求：包含 **MySQL 数据库建模与造数**、**Java 后端应用程序**、以及 **带 GUI 的前端页面**（HTML/CSS/JavaScript）。

---

## 功能概览（对应 deliverable 3）

主菜单（`frontend/index.html`）包含：
- Account Info
- Book an Appointment
- Create a Wellness Challenge
- Monthly Health Summary
- Search Records
- Sign Out

已实现的主要功能：
- **账户信息**：修改姓名；增删邮箱；增删/更新手机号；链接/解绑 Provider；设置/清除 Primary Care Provider
- **预约**：创建预约；取消预约并记录取消原因（**后端校验：必须在预约时间 24 小时之前才能取消**）
- **挑战**：创建挑战；加入挑战；更新进度/标记完成；发送邀请（EMAIL/PHONE）；记录邀请发起/到期时间
- **统计与搜索**：
  - 指定日期范围内预约总数
  - 指定 metric 的月度 avg/min/max
  - 参与人数最多的挑战
  - 最活跃用户（health_record 数量 + 完成挑战数）
  - 按 health_id/provider/type/date range 搜索预约；按 metric/date range 搜索健康记录

额外实现（非必做，但更贴近“可使用的系统”）：
- **Provider 登录与门户**：Provider 可使用 `license_no + 登录码` 登录，查看/取消自己的预约（原因入库）

---

## 项目结构

- `sql/`
  - `01_schema.sql`：建库建表（PK/FK/UNIQUE/索引）
  - `02_seed.sql`：造数脚本（每表足量数据，覆盖取消预约/邀请过期/挑战参与/健康记录等场景）
- `backend/`：Spring Boot 后端（REST API + JDBC）
  - `src/main/resources/application.properties`：MySQL 连接配置
- `frontend/`：静态前端（HTML/CSS/JS，通过 `fetch` 调用后端 API）
  - `assets/api.js`：API 封装，默认后端地址 `http://localhost:8080/api`
- `docs/`（详见 `docs/README.md`）
  - 规范/报告：`Requirement/project_deliverable_1.md`、`Requirement/project_deliverable_2.md`、`Requirement/project_deliverable_3.md`、`Requirement/project_description.md`、`Final-Report.md`
  - 设计/演示：`code_overview.md`、`feature_manual_detailed.md`、`demo_playbook.md`
  - 运行/提交辅助（Detail）：`Detail/api_reference.md`、`Detail/db_schema_detail.md`、`Detail/seed_data_overview.md`、`Detail/run_and_troubleshoot.md`、`Detail/screenshots_guide.md`、`Detail/curl_samples.sh`

---

## 环境依赖

- **MySQL**：8.x（本地 `localhost:3306`）
- **Java**：17+
- **Maven**：3.8+
- 浏览器：Chrome / Edge / Firefox 任意

---

## 运行方式

### 快速一键（推荐）

```bash
cd /work/healthtrack2
./scripts/install.sh          # 安装依赖、启动 MySQL、建库+造数、生成 .env.local、预热 Maven
./scripts/start.sh --frontend # 启动后端，顺便用 python 起静态前端（默认 4173）
```

入口：
- 前端：`http://localhost:4173/login.html`（或直接打开 `frontend/login.html`）
- 后端：`http://localhost:8080/api`

登录：
- 用户：`userId=1..6`（或新注册）
- Provider：`license_no=LIC-10001..10004`，登录码 `provider123`

### 手动运行
1) MySQL 执行 `sql/01_schema.sql` → `sql/02_seed.sql`  
2) 配置 `backend/src/main/resources/application.properties` 或 `scripts/.env.local`（数据库密码等）  
3) `cd backend && mvn spring-boot:run`（默认端口 8080）  
4) 打开 `frontend/login.html` 或使用 `./scripts/start.sh --frontend`

---

## 常用演示建议（便于截图）

- **用户选择**：在 `index.html` 选择 `userId=1`
- **Account Info**：修改姓名、加/删邮箱、更新/删除 phone、链接 provider、设置 primary care
- **Appointment**：创建一个“2 天后”的预约 → 再取消并输入 reason（注意必须大于 24 小时）
- **Challenge**：创建挑战 → Select → Join → 更新 progress → Invite EMAIL/PHONE → 查看 invitations
- **Summary/Search**：选择一个月份和 metric（如 `steps`）查看 avg/min/max；搜索预约/健康记录

更多演示提示：
- 演示步骤：`docs/demo_playbook.md`
- 全功能 + 示例：`docs/feature_manual_detailed.md`
- 运行/排障：`docs/Detail/run_and_troubleshoot.md`

---

## 常见问题排查

- **后端连不上 MySQL**
  - 检查 MySQL 是否运行、端口是否为 3306
  - 检查 `application.properties` 的用户名/密码
  - 确认 MySQL 中存在数据库 `healthtrack` 且已执行 `01_schema.sql`

- **前端报 CORS 或 fetch 失败**
  - 确认后端已启动在 `localhost:8080`
  - `frontend/assets/api.js` 的 `API_BASE` 默认为 `http://localhost:8080/api`

- **取消预约失败**
  - 系统规定：**预约开始前 24 小时内不可取消**（后端会返回错误信息）

- **容器/无 sudo**
  - 脚本已支持 root 直接执行；必要时手动 `service mysql start`

---

## 关键文档
- 规范/报告：`docs/Requirement/project_deliverable_*.md`、`docs/Requirement/project_description.md`、`docs/Final-Report.md`
- 设计/演示：`docs/code_overview.md`、`docs/feature_manual_detailed.md`、`docs/demo_playbook.md`
- 运行/提交辅助：`docs/Detail/api_reference.md`、`docs/Detail/db_schema_detail.md`、`docs/Detail/seed_data_overview.md`、`docs/Detail/run_and_troubleshoot.md`、`docs/Detail/screenshots_guide.md`、`docs/Detail/curl_samples.sh`
