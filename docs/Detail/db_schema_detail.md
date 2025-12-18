# Database Schema Detail（表级约束与关系）

> 建表 SQL 位于 `sql/01_schema.sql`；此处按表总结用途、键、索引与约束。

## 用户与联系人
- `user_account`
  - PK: `id`
  - UK: `health_id`
  - FK: `primary_provider_id` → `provider(id)` (ON DELETE SET NULL)
- `user_email`
  - PK: `id`
  - UK: `email`
  - FK: `user_id` → `user_account(id)` (CASCADE)
- `user_phone`
  - PK: `user_id`
  - UK: `phone`
  - FK: `user_id` → `user_account(id)` (CASCADE)

## Provider
- `provider`
  - PK: `id`
  - UK: `license_no`, `email`
- `provider_account`
  - PK/FK: `provider_id` → `provider(id)` (CASCADE)
- `user_provider_link`
  - PK: (`user_id`, `provider_id`)
  - FK: `user_id` → `user_account(id)` (CASCADE); `provider_id` → `provider(id)` (RESTRICT on delete)

## 家庭
- `family_group`
  - PK: `id`
- `family_group_member`
  - PK: (`family_group_id`, `user_id`)
  - FK: `family_group_id` → `family_group(id)` (CASCADE); `user_id` → `user_account(id)` (CASCADE)

## 健康指标
- `metric_type`
  - PK: `id`
  - UK: `code`
- `health_record`
  - PK: `id`
  - IDX: (`user_id`, `recorded_at`), (`metric_type_id`, `recorded_at`)
  - FK: `user_id` → `user_account(id)` (CASCADE); `metric_type_id` → `metric_type(id)` (RESTRICT)

## 预约
- `appointment`
  - PK: `id`
  - IDX: (`user_id`, `scheduled_at`), (`provider_id`, `scheduled_at`)
  - FK: `user_id` → `user_account(id)` (CASCADE); `provider_id` → `provider(id)` (RESTRICT)
  - ENUM: `appointment_type` (IN_PERSON/VIRTUAL), `status` (SCHEDULED/CANCELLED/COMPLETED)
- `appointment_cancellation`
  - PK/FK: `appointment_id` → `appointment(id)` (CASCADE)

## 挑战与邀请
- `challenge`
  - PK: `id`
  - FK: `creator_user_id` → `user_account(id)` (CASCADE)
- `challenge_participant`
  - PK: (`challenge_id`, `user_id`)
  - FK: `challenge_id` → `challenge(id)` (CASCADE); `user_id` → `user_account(id)` (CASCADE)
- `invitation`
  - PK: `id`
  - UK: (`challenge_id`, `recipient_type`, `recipient_value`)  // 防重复邀请
  - FK: `challenge_id` → `challenge(id)` (CASCADE); `sender_user_id` → `user_account(id)` (CASCADE); `recipient_user_id` → `user_account(id)` (SET NULL)

## 约束与关系概要
- 唯一性：health_id / license_no / provider.email / user_email.email / user_phone.phone / metric_type.code / invitation.recipient 组合。
- 主要关系：
  - User ↔ Provider（M:N）+ 主治（user_account.primary_provider_id）
  - User ↔ Email (1:N)，User ↔ Phone (1:1)
  - User ↔ Family (M:N with role/can_manage)
  - User ↔ Appointment ↔ Provider（1:1:1），取消 1:1 映射至 appointment_cancellation
  - User ↔ Challenge (creator)，Challenge ↔ Participant (M:N)，Challenge ↔ Invitation (1:N，收件人可空)
  - User ↔ HealthRecord ↔ MetricType（1:N, 1:N）
