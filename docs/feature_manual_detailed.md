# HealthTrack 全功能详解与实操案例（详细版）

本手册按功能逐项给出使用方式与实际操作示例（含 curl 请求），方便演示与回归测试。默认后端地址 `http://localhost:8080/api`，前端静态页 `http://localhost:4173/login.html`（运行 `./scripts/start.sh --frontend` 时）。示例账号来自 `sql/02_seed.sql`：
- 用户：`userId=1..6`，示例 `userId=1`（health_id=H-0001, 姓名 Xiao YiWei）。
- Provider：`id=1..4`，示例 `license_no=LIC-10001`，登录码 `provider123`。

---

## 1. 登录与导航
- 用户登录：前端登录页输入 `userId=1`，进入 `index.html` 主菜单。
- Provider 登录：登录页选择 Provider，输入 `LIC-10001` + `provider123`，进入 `provider.html`。

---

## 2. 账户管理（Account）
- 查看账户信息（含手机号/邮箱/已绑定 Provider）  
  ```bash
  curl http://localhost:8080/api/account/1
  ```
- 创建用户（health_id/邮箱/手机号全局唯一）  
  ```bash
  curl -X POST http://localhost:8080/api/account/create \
    -H "Content-Type: application/json" \
    -d '{"healthId":"H-0099","fullName":"Demo User","email":"demo@example.com","phone":"555-9998"}'
  ```
- 修改姓名  
  ```bash
  curl -X PUT http://localhost:8080/api/account/update \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"fullName":"Xiao YiWei Jr."}'
  ```
- 邮箱增删（重复邮箱会 409）  
  ```bash
  curl -X POST http://localhost:8080/api/account/email/add \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"email":"new.mail@example.com"}'
  curl -X POST http://localhost:8080/api/account/email/remove \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"email":"xiaoyialt@example.com"}'
  ```
- 手机更新/删除（幂等；更新会重置验证状态）  
  ```bash
  curl -X POST http://localhost:8080/api/account/phone/upsert \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"phone":"555-7777"}'
  curl -X POST http://localhost:8080/api/account/phone/remove \
    -H "Content-Type: application/json" \
    -d '{"userId":1}'
  ```
- Provider 相关  
  - 列出可选 Provider：`curl http://localhost:8080/api/account/providers`
  - 绑定 / 解绑：  
    ```bash
    curl -X POST http://localhost:8080/api/account/provider/link \
      -H "Content-Type: application/json" \
      -d '{"userId":1,"providerId":1}'
    curl -X POST http://localhost:8080/api/account/provider/unlink \
      -H "Content-Type: application/json" \
      -d '{"userId":1,"providerId":2}'
    ```
  - 设为主治 / 清除主治（providerId 可为 null 以清空）：  
    ```bash
    curl -X POST http://localhost:8080/api/account/provider/set-primary \
      -H "Content-Type: application/json" \
      -d '{"userId":1,"providerId":1}'
    curl -X POST http://localhost:8080/api/account/provider/set-primary \
      -H "Content-Type: application/json" \
      -d '{"userId":1,"providerId":null}'
    ```

---

## 3. 预约管理（Appointment）
- 创建预约（用户需已绑定该 Provider；类型只能 `IN_PERSON` / `VIRTUAL`）  
  ```bash
  curl -X POST http://localhost:8080/api/appointment/create \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"providerId":1,"scheduledAt":"2025-01-20T10:00:00","appointmentType":"IN_PERSON","memo":"annual check"}'
  ```
- 用户取消预约（仅限 SCHEDULED 且需早于预约时间 24h）  
  ```bash
  curl -X POST http://localhost:8080/api/appointment/cancel \
    -H "Content-Type: application/json" \
    -d '{"appointmentId":1001,"reason":"change plan"}'
  ```
  - 失败示例：若预约时间距当前不足 24h，会返回 400 “Cannot cancel within 24 hours of scheduled time.”
- Provider 取消（无 24h 限制，需是自己的预约）  
  ```bash
  curl -X POST http://localhost:8080/api/provider/appointment/cancel \
    -H "Content-Type: application/json" \
    -d '{"providerId":1,"appointmentId":1002,"reason":"Unavailable"}'
  ```
- 搜索预约（按用户 health_id、providerId、类型、时间范围）  
  ```bash
  curl -X POST http://localhost:8080/api/appointment/search \
    -H "Content-Type: application/json" \
    -d '{"healthId":"H-0001","providerId":1,"appointmentType":"IN_PERSON","start":"2025-01-01T00:00:00","end":"2025-02-01T00:00:00"}'
  ```

---

## 4. 健康记录（Health Record）
- 列出指标类型（种子含 weight / bp_systolic / bp_diastolic / steps）  
  ```bash
  curl http://localhost:8080/api/health-record/metric-types
  ```
- 录入健康记录（metricCode 必须存在，否则 400）  
  ```bash
  curl -X POST http://localhost:8080/api/health-record/create \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"metricCode":"weight","metricValue":61.5,"recordedAt":"2025-01-18T08:00:00","note":"morning"}'
  ```
- 搜索健康记录（跨用户；按 health_id / metric / 时间段）  
  ```bash
  curl "http://localhost:8080/api/search/health-records?healthId=H-0001&metricCode=steps&start=2024-12-01T00:00:00&end=2025-02-01T00:00:00"
  ```

---

## 5. 挑战与邀请（Challenge）
- 创建挑战（结束日期需 >= 开始日期）  
  ```bash
  curl -X POST http://localhost:8080/api/challenge/create \
    -H "Content-Type: application/json" \
    -d '{"creatorUserId":1,"goalText":"10k steps for 7 days","startDate":"2025-01-15","endDate":"2025-01-22"}'
  ```
- 列表 / 我的挑战  
  ```bash
  curl http://localhost:8080/api/challenge/list
  curl "http://localhost:8080/api/challenge/my?userId=1"
  ```
- 加入挑战（幂等，重复加入不会报错）  
  ```bash
  curl -X POST "http://localhost:8080/api/challenge/join?challengeId=2001&userId=2"
  ```
- 更新进度 / 标记完成  
  ```bash
  curl -X POST http://localhost:8080/api/challenge/progress \
    -H "Content-Type: application/json" \
    -d '{"challengeId":2001,"userId":2,"progressValue":75.0,"isCompleted":false}'
  curl -X POST http://localhost:8080/api/challenge/progress \
    -H "Content-Type: application/json" \
    -d '{"challengeId":2001,"userId":1,"progressValue":100.0,"isCompleted":true}'
  ```
- 查看参与者列表  
  ```bash
  curl http://localhost:8080/api/challenge/2001/participants
  ```
- 发送邀请（EMAIL/PHONE；若目标邮箱/手机号已存在用户，会自动关联 recipient_user_id）  
  ```bash
  curl -X POST http://localhost:8080/api/challenge/invite \
    -H "Content-Type: application/json" \
    -d '{"challengeId":2001,"senderUserId":1,"recipientType":"EMAIL","recipientValue":"friend@example.com"}'
  curl -X POST http://localhost:8080/api/challenge/invite \
    -H "Content-Type: application/json" \
    -d '{"challengeId":2001,"senderUserId":1,"recipientType":"PHONE","recipientValue":"555-1234"}'
  ```
- 查看邀请列表 / 按参与人数排名的挑战  
  ```bash
  curl http://localhost:8080/api/challenge/2001/invitations
  curl "http://localhost:8080/api/challenge/top?limit=5"
  ```

---

## 6. 家庭视图（Family）
- 家庭成员列表（示例 userId=1 与 userId=5 同属“Wei Family”）  
  ```bash
  curl http://localhost:8080/api/family/1/members
  ```
- 查看成员健康记录（需同一家庭组；limit 默认 50，最大 200）  
  ```bash
  curl -X POST "http://localhost:8080/api/family/member/health-records?limit=20" \
    -H "Content-Type: application/json" \
    -d '{"viewerUserId":1,"targetUserId":5}'
  ```
  - 权限失败示例：`viewerUserId=1, targetUserId=2` 会返回 400 “无权限查看：对方不是你的家庭成员。”
- 查看成员挑战  
  ```bash
  curl -X POST http://localhost:8080/api/family/member/challenges \
    -H "Content-Type: application/json" \
    -d '{"viewerUserId":1,"targetUserId":5}'
  ```

---

## 7. 统计与搜索（Summary & Search）
- 预约数量（按 scheduled_at 的日期范围，含首尾）  
  ```bash
  curl -X POST http://localhost:8080/api/summary/appointment-count \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"startDate":"2025-01-01","endDate":"2025-01-31"}'
  ```
- 月度指标统计（avg/min/max）  
  ```bash
  curl -X POST http://localhost:8080/api/summary/metric-stats \
    -H "Content-Type: application/json" \
    -d '{"userId":1,"metricCode":"steps","monthStart":"2025-01-01","monthEnd":"2025-01-31"}'
  ```
- 最活跃用户排行榜（记录数 + 已完成挑战数之和，limit 默认 5）  
  ```bash
  curl "http://localhost:8080/api/summary/top-active-users?limit=10"
  ```
- 健康记录搜索：见第 4 节；预约搜索：见第 3 节。

---

## 8. Provider 门户相关
- 登录  
  ```bash
  curl -X POST http://localhost:8080/api/provider/login \
    -H "Content-Type: application/json" \
    -d '{"licenseNo":"LIC-10001","loginCode":"provider123"}'
  ```
- 查看自己的预约 / 取消预约  
  ```bash
  curl http://localhost:8080/api/provider/1/appointments
  curl -X POST http://localhost:8080/api/provider/appointment/cancel \
    -H "Content-Type: application/json" \
    -d '{"providerId":1,"appointmentId":1002,"reason":"Provider sick"}'
  ```
- 患者列表 / 查看患者健康记录（仅限与该 Provider 绑定的用户）  
  ```bash
  curl http://localhost:8080/api/provider/1/patients
  curl "http://localhost:8080/api/provider/1/patients/1/health-records?limit=30"
  ```

---

## 9. 前端页面映射（便于演示）
- `login.html`：用户/Provider 登录入口。
- `index.html`：用户主菜单。
- `pages/account.html`：账户信息（姓名/邮箱/手机/Provider/主治）。
- `pages/appointment.html`：创建/取消/搜索预约。
- `pages/health_record.html`：指标类型、录入健康记录。
- `pages/challenge.html`：创建/加入/进度、邀请、Top 列表。
- `pages/family.html`：家庭成员、查看成员健康记录/挑战。
- `pages/summary.html`：预约统计、月度指标、活跃用户。
- `pages/search.html`：健康记录搜索。
- `provider.html`：Provider 预约列表、取消、患者列表、查看患者健康记录。

---

## 10. 常见校验与错误提示
- 重复邮箱/手机号/health_id：返回 409 “Duplicate value violates unique constraint.”
- 预约类型必须 `IN_PERSON` / `VIRTUAL`，否则 400。
- 预约取消规则：用户端预约需距当前 ≥24h；Provider 端无限制。
- 预约创建需先绑定 Provider，否则 400 “You can only book appointments with providers linked to your account.”
- 邀请类型必须 `EMAIL` 或 `PHONE`；健康指标 metricCode 必须存在于 `metric_type`。
- 家庭/Provider 视图的权限检查失败会返回 400，并给出无权限原因。

---

## 11. 组合演示参考（从 0 到完整体验）
1) 用户 1 登录 → Account 页更新邮箱/手机并绑定 Provider=1 → 设主治。  
2) 创建预约（+3 天）→ 立即取消并填写原因，验证 24h 规则。  
3) 录入一条体重与步数记录，随后在 Summary 查看 steps 的月度统计。  
4) 创建挑战 → 用户 2 加入并更新进度 → 用户 1 标记完成 → 发送 EMAIL 邀请 → 查看 Top 挑战。  
5) 在 Family 页用用户 1 查看成员 5 的健康记录与挑战。  
6) Provider 1 登录 → 查看预约 → 取消一条预约并查看患者健康记录。  
7) 使用 Search/Appointment 搜索接口按 health_id 和时间段拉取记录，验证数据一致性。
