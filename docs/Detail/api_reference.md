# HealthTrack API Reference（Phase 3 实现版）

> 所有接口基于 `http://localhost:8080/api`，请求体使用 JSON（UTF-8），时间戳采用 ISO-8601（示例：`2025-01-20T10:00:00`）。前端调用集中在 `frontend/assets/api.js`。

## Account (`/api/account`)
- `POST /create` —— 创建用户 `{ healthId, fullName, email, phone }` → `UserSummary`
- `GET /{userId}` —— 获取账户信息（姓名/邮箱/手机/已绑定 Provider）
- `PUT /update` —— 修改姓名 `{ userId, fullName }`
- `POST /email/add` —— 添加邮箱 `{ userId, email }`
- `POST /email/remove` —— 删除邮箱 `{ userId, email }`
- `POST /phone/upsert` —— 更新/新增手机号 `{ userId, phone }`
- `POST /phone/remove` —— 删除手机号 `{ userId }`
- `POST /provider/link` —— 绑定 Provider `{ userId, providerId }`
- `POST /provider/unlink` —— 解绑 Provider `{ userId, providerId }`
- `POST /provider/set-primary` —— 设置/清除主治 `{ userId, providerId|null }`
- `GET /providers` —— 列出全部 Provider

## Appointment (`/api/appointment`)
- `POST /create` —— 创建预约 `{ userId, providerId, scheduledAt, appointmentType(IN_PERSON|VIRTUAL), memo? }` → appointmentId
- `POST /cancel` —— 用户取消 `{ appointmentId, reason }`（需距离预约 ≥24h；状态必须 SCHEDULED）
- `POST /search` —— 搜索预约 `{ healthId?, providerId?, appointmentType?, start?, end? }`

## Challenge (`/api/challenge`)
- `POST /create` —— 创建挑战 `{ creatorUserId, goalText, startDate, endDate }`
- `GET /list` —— 全部挑战
- `GET /my?userId=` —— 某用户参与的挑战
- `POST /join?challengeId=&userId=` —— 加入挑战（幂等）
- `POST /progress` —— 更新进度 `{ challengeId, userId, progressValue, isCompleted }`
- `GET /{challengeId}/participants` —— 参与者列表
- `POST /invite` —— 发送邀请 `{ challengeId, senderUserId, recipientType(EMAIL|PHONE), recipientValue }`
- `GET /{challengeId}/invitations` —— 邀请列表
- `GET /top?limit=5` —— 按参与人数排序的 Top 挑战

## Health Record (`/api/health-record`)
- `GET /metric-types` —— 指标列表（code/name/unit）
- `POST /create` —— 录入健康记录 `{ userId, metricCode, recordedAt, metricValue, note? }` → recordId

## Family (`/api/family`)
- `GET /{userId}/members` —— 家庭成员列表（含角色/权限）
- `POST /member/health-records?limit=50` —— 查看成员健康记录 `{ viewerUserId, targetUserId }`
- `POST /member/challenges` —— 查看成员挑战 `{ viewerUserId, targetUserId }`

## Search (`/api/search`)
- `GET /health-records` —— 按 healthId/metricCode/时间段搜索健康记录  
  参数：`healthId?`, `metricCode?`, `start?` (ISO_DATETIME), `end?`

## Summary (`/api/summary`)
- `POST /appointment-count` —— 预约数量 `{ userId, startDate, endDate }`
- `POST /metric-stats` —— 月度指标统计 `{ userId, metricCode, monthStart, monthEnd }` → avg/min/max
- `GET /top-active-users?limit=5` —— 最活跃用户（记录数 + 已完成挑战数）

## Provider (`/api/provider`)
- `POST /login` —— Provider 登录 `{ licenseNo, loginCode }` → Provider 信息
- `GET /{providerId}/appointments` —— 该 Provider 的预约列表
- `POST /appointment/cancel` —— Provider 取消预约 `{ providerId, appointmentId, reason }`（无限制）
- `GET /{providerId}/patients` —— 患者列表
- `GET /{providerId}/patients/{userId}/health-records?limit=50` —— 患者健康记录（仅限绑定患者）
