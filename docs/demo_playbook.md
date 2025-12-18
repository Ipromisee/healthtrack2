# HealthTrack 功能展示示例手册

## 0. 前置准备
- 运行 `./scripts/install.sh` 生成数据并安装依赖；`./scripts/start.sh --frontend` 启动后端（8080）并起静态前端（4173）。
- 前端地址：`http://localhost:4173/login.html`（或直接双击 `frontend/login.html` 但需后端已运行）。
- 种子账号（见 `sql/02_seed.sql`）：
  - 用户：`userId=1..6`（示例：1 -> Xiao YiWei, health_id=H-0001）
  - Provider：`license_no=LIC-10001..LIC-10004`，登录码 `provider123`

## 1. 登录与导航
- 用户登录：在登录页输入 `userId=1`，进入 `index.html` 主菜单。
- Provider 登录：输入 `LIC-10001` + `provider123`，跳转 `provider.html`。

## 2. 账户信息（Account）
目标：改名、管理邮箱/手机、绑定 Provider、设主治。
1) 进入 `Account Info`；右上角切换 `userId=1`。
2) 修改姓名：输入新名字点击保存。
3) 邮箱：添加 `new.mail@example.com`；删除未验证邮箱 `xiaoyialt@example.com`。
4) 手机：更新为 `555-7777`。
5) Provider：在下拉框选择 `Dr. Alice Chen`，点击 Link；再选择“Set Primary Provider”。

示例 API（改名）：
```bash
curl -X PUT http://localhost:8080/api/account/update \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"fullName":"Demo User"}'
```

## 3. 预约（Appointment）
目标：创建预约、验证 24 小时取消限制、搜索预约。
1) 创建：在 `Book an Appointment` 选择 Provider=1，类型=IN_PERSON，时间=未来 3 天，提交成功会返回预约 ID。
2) 取消：在列表选择刚创建的预约，输入取消原因。注意只有距离预约 ≥24h 才允许取消。
3) 搜索：按日期范围筛选，或按 Provider/type 组合搜索。

示例 API（创建预约，memo 可选）：
```bash
curl -X POST http://localhost:8080/api/appointment/create \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"providerId":1,"scheduledAt":"2025-01-10T10:00:00","appointmentType":"IN_PERSON","memo":"demo"}'
```

## 4. 健康记录（Health Record）
目标：新增指标、查看指标类型、验证列表更新。
1) 在 `Health Record` 页点击“Load Metric Types”看到 codes：`weight`, `bp_systolic`, `bp_diastolic`, `steps`。
2) 录入：为 userId=1 录入 `metricCode=weight`, `metricValue=61.5`, `recordedAt=当前时间`。
3) 列表会出现新增记录；再试 steps 录入。

示例 API（录入体重）：
```bash
curl -X POST http://localhost:8080/api/health-record/create \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"metricCode":"weight","metricValue":61.5,"recordedAt":"2025-01-08T08:00:00","note":"morning"}'
```

## 5. 挑战（Challenge）
目标：创建挑战、加入、更新进度、邀请。
1) 创建：在 `Create a Wellness Challenge` 输入目标 “10k steps/day for 7 days”，选择开始=今天，结束=+7 天。
2) 加入：切换用户 2（或 5）在 `Challenge` 页点击 Join。
3) 更新进度：用户 2 输入 progress=30，标记未完成；用户 1 标记完成，观察 completed_at。
4) 邀请：用户 1 选择 EMAIL，输入 `friend@example.com` 发送；查看 Invitations。
5) 查看排行榜：`Top Challenges` 按参与人数排序。

示例 API（邀请邮件）：
```bash
curl -X POST http://localhost:8080/api/challenge/invite \
  -H "Content-Type: application/json" \
  -d '{"challengeId":2001,"senderUserId":1,"recipientType":"EMAIL","recipientValue":"friend@example.com"}'
```

## 6. 统计与搜索（Summary & Search）
目标：验证统计接口、搜索健康记录。
- 预约数：在 `Monthly Health Summary` 选择 userId=1，日期范围覆盖当月，查看 appointmentCount。
- 月度指标：选择 metric=steps，返回 avg/min/max。
- 最活跃用户：点击“Top Active Users”查看返回列表（活跃度=健康记录数+完成挑战数）。
- 搜索健康记录：在 `Search Records` 以 `health_id=H-0001`，metric=steps，选择日期范围，查看结果。

示例 API（月度统计）：
```bash
curl -X POST http://localhost:8080/api/summary/metric-stats \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"metricCode":"steps","monthStart":"2025-01-01","monthEnd":"2025-01-31"}'
```

## 7. 家庭视图（Family）
目标：验证家庭成员权限、查看成员数据。
- userId=1 打开 `Family` 页，加载成员（含 userId=5）。
- 查看成员挑战/健康记录：对成员点击“View Health Records”或“View Challenges”；非同一家庭的用户会收到“无权限”提示。

示例 API（查看成员健康记录，限制同组）：
```bash
curl -X POST http://localhost:8080/api/family/member/health-records \
  -H "Content-Type: application/json" \
  -d '{"viewerUserId":1,"targetUserId":5,"limit":20}'
```

## 8. Provider 门户
目标：Provider 登录、查看预约/患者、取消预约、查看患者健康记录。
1) 登录：`license_no=LIC-10001`，`login_code=provider123`。
2) 预约列表：查看未来预约，尝试“Cancel”填写原因（Provider 端不受 24h 限制）。
3) 患者列表：点击某患者查看近期健康记录（仅限自己患者）。

示例 API（Provider 取消预约）：
```bash
curl -X POST http://localhost:8080/api/provider/appointment/cancel \
  -H "Content-Type: application/json" \
  -d '{"providerId":1,"appointmentId":1002,"reason":"Unavailable"}'
```

## 9. 常见检查点
- 预约取消：用户端若离预约不足 24h，应返回 400 提示；Provider 端可随时取消。
- 权限：家庭/Provider 查看他人数据时若无权限，应返回 400 并提示。
- CORS：前端通过浏览器访问需后端已启；默认允许跨域。
- 数据一致性：重复邮箱/手机号/health_id 会返回 409（唯一键冲突）。
