# Runbook & Troubleshooting

本文件用于说明如何在本地或容器中运行项目，并给出常见问题排查方法。

## 快速启动（推荐）
```bash
cd /work/healthtrack2
./scripts/install.sh
./scripts/start.sh --frontend
```
访问：
- 前端：`http://localhost:4173/login.html`
- 后端：`http://localhost:8080/api`

## 手动启动
1) 执行建表与造数：
```bash
mysql -uroot < sql/01_schema.sql
mysql -uroot < sql/02_seed.sql
```
2) 配置 DB：
- `backend/src/main/resources/application.properties` 或 `scripts/.env.local`
3) 启动后端：
```bash
cd backend
mvn spring-boot:run
```
4) 打开前端：
- 直接打开 `frontend/login.html`，或
- `./scripts/start.sh --frontend` 以静态服务方式访问

## 容器环境常见问题
- **没有 sudo**：脚本已支持 root 直接执行。确保 `service mysql start` 可用。
- **MySQL 无法连接**：先执行 `service mysql start`；如有 root 密码，设置 `MYSQL_ROOT_PASSWORD`：
  ```bash
  MYSQL_ROOT_PASSWORD=yourpass ./scripts/install.sh
  ```
- **端口被占用**：`SERVER_PORT=8081 ./scripts/start.sh` 或修改 `scripts/.env.local`
- **前端 CORS**：后端 `CorsConfig` 已允许跨域；确保后端已启动。

## 健康检查
```bash
mysqladmin ping -uroot
curl http://localhost:8080/api/account/providers
```

## 典型错误与解释
- **预约取消失败**：若距预约不足 24 小时，返回 400（用户侧规则）。
- **权限不足**：家庭/Provider 查看他人记录时返回 400（不在同一家庭组或不是 Provider 患者）。
- **唯一键冲突**：重复 health_id / email / phone / license_no 会返回 409。
