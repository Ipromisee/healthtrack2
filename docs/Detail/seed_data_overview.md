# Seed Data Overview（造数覆盖与验证）

> 造数脚本：`sql/02_seed.sql`。执行前确保已运行 `sql/01_schema.sql` 创建表。

## 覆盖的实体与典型数量
- 用户 `user_account`：6 条（health_id H-0001..H-0006，示例姓名 Xiao YiWei 等）
- Provider `provider`：4 条（license_no LIC-10001..10004，登录码统一 `provider123`）
- Provider 绑定 `user_provider_link`：10+ 绑定，含多 Provider 用户
- 邮箱 `user_email`：8 条（含已验证/未验证混合）
- 手机 `user_phone`：6 条（含已验证/未验证混合）
- 家庭组 `family_group`：2 组；成员 `family_group_member`：至少 4 条，含 role / can_manage
- 预约 `appointment`：示例 5 条，包含 SCHEDULED/COMPLETED/CANCELLED；取消原因记录在 `appointment_cancellation`
- 挑战 `challenge`：3 条；参与者 `challenge_participant`：7+ 条（含完成/未完成）
- 邀请 `invitation`：4 条（PENDING / ACCEPTED / EXPIRED 覆盖）
- 指标 `metric_type`：4 条（weight / bp_systolic / bp_diastolic / steps）
- 健康记录 `health_record`：多月分布，涵盖不同用户与指标，用于月度统计和“最活跃用户”

## 验证建议（快速 SQL）
```sql
-- 核查基础表行数
SELECT COUNT(*) FROM user_account;
SELECT COUNT(*) FROM provider;
SELECT COUNT(*) FROM appointment;
SELECT COUNT(*) FROM challenge;
SELECT COUNT(*) FROM invitation;
SELECT COUNT(*) FROM health_record;

-- 检查唯一性冲突：不应返回结果
SELECT email, COUNT(*) FROM user_email GROUP BY email HAVING COUNT(*)>1;
SELECT phone, COUNT(*) FROM user_phone GROUP BY phone HAVING COUNT(*)>1;
SELECT license_no, COUNT(*) FROM provider GROUP BY license_no HAVING COUNT(*)>1;

-- 示例：月度统计所需的记录分布
SELECT metric_type_id, COUNT(*) FROM health_record GROUP BY metric_type_id;
SELECT DATE(recorded_at) d, COUNT(*) FROM health_record GROUP BY d ORDER BY d DESC LIMIT 10;

-- 邀请状态覆盖
SELECT status, COUNT(*) FROM invitation GROUP BY status;
```

## 演示场景提示
- 用户登录：`userId=1`（health_id=H-0001）或 `userId=2..6`
- Provider 登录：`license_no=LIC-10001` + `provider123`
- 预约取消规则：预约时间需距当前 ≥24h，否则 API 返回 400
- 挑战邀请：已包含 pending/accepted/expired，可直接在前端查看
- 家庭视图：userId=1 与 userId=5 同组，可互看；对非同组会提示无权限
