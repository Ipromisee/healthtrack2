# HealthTrack 代码框架与功能概览

## 1. 项目结构与技术栈
- `backend/`：Spring Boot 3（Java 17）+ Spring JDBC，REST API，Maven 构建。
- `frontend/`：纯静态 HTML/CSS/JS，`assets/api.js` 通过 `fetch` 调用后端 `http://localhost:8080/api`（可在文件内调整）。
- `sql/`：`01_schema.sql` 定义 MySQL 8 表结构，`02_seed.sql` 造数（用户/Provider/预约/挑战/健康记录等）。
- `scripts/`：`install.sh`（安装依赖、建库/造数）、`start.sh`（启动后端，可选启动静态前端）。
- `docs/`：作业文档与本概览。

## 2. 后端分层
- Controller（`controller/`）：声明 REST 路由，做简单参数校验。
- Service（`service/`）：业务规则，如预约 24 小时前方可取消、家庭成员/Provider 权限校验、挑战邀请类型判断等。
- Repository（`repo/`）：使用 `JdbcTemplate` 直接写 SQL，映射到 DTO。
- DTO（`dto/`）：请求/响应与行模型，便于跨层传递。
- Config（`config/`）：`CorsConfig` 开放跨域；`ApiExceptionHandler` 统一 4xx/5xx 响应格式。
- Util（`util/SqlUtil`）：常用 JDBC 帮助函数。
- 配置：`backend/src/main/resources/application.properties` 提供默认的 datasource；`scripts/.env.local` 可覆盖。

## 3. 数据模型要点（MySQL）
- 账户：`user_account`（health_id 唯一，primary_provider_id 可为空）、`user_email`（全局唯一）、`user_phone`（全局唯一）、`user_provider_link`（多对多绑定 Provider）。
- Provider：`provider`（基础信息）、`provider_account`（登录码）。
- 预约：`appointment`（SCHEDULED/COMPLETED/CANCELLED）、`appointment_cancellation`（取消记录；用户侧需提前 24h）。
- 挑战：`challenge`、`challenge_participant`（进度/完成状态）、`invitation`（EMAIL/PHONE，含过期时间）。
- 健康记录：`metric_type`（如 weight/steps/bp_*）、`health_record`（metric_value、recorded_at）。
- 家庭：`family_group`、`family_group_member`（成员角色/可管理权限）。

## 4. 主要 API（按模块）
- `/api/account`：创建用户、查账户信息、改名、增删邮箱/手机、链接/解绑 Provider、设置主治、列出可用 Provider。
- `/api/appointment`：创建预约、用户侧取消（需 ≥24h 前）、条件搜索。
- `/api/challenge`：创建挑战、列表/我的挑战、加入、更新进度、查看参与者、发送邀请（EMAIL/PHONE）、查看邀请、Top 挑战。
- `/api/health-record`：列指标类型、录入健康记录。
- `/api/family`：查看家庭成员、查看成员健康记录/挑战（同一家庭组才允许）。
- `/api/search`：按 health_id/metric/date 范围搜索健康记录。
- `/api/summary`：预约数量统计、按月度 metric 统计、最活跃用户。
- `/api/provider`：Provider 登录、查看/取消自己的预约、查看患者列表、查看患者健康记录（限自己患者）。

## 5. 前端页面
- `frontend/login.html`：用户登录（输入 userId）或 Provider 登录（license_no + 登录码）。
- `frontend/index.html`：用户主菜单，链接到各功能页。
- `frontend/pages/*.html`：Account、Appointment、Challenge、Family、Health Record、Search、Summary。
- `frontend/provider.html`：Provider 门户（预约列表、患者列表、查看健康记录、取消预约）。

## 6. 运行与环境
- 推荐：`./scripts/install.sh`（安装 Java/Maven/MySQL、建库+造数、生成 `scripts/.env.local`）；`./scripts/start.sh --frontend`（启动后端并用 python 起静态前端）。
- 默认后端端口 `8080`，前端静态服端口 `4173`（可在 `start.sh` 参数中调整）。
