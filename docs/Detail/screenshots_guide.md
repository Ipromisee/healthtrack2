# Screenshots / Use Printout Guide

> 用于提交“程序使用打印/截图”时的命名与覆盖点。

## 建议截图清单
1. `01_main_menu_user_selection.png` —— `frontend/index.html` 选择 userId=1
2. `02_account_info.png` —— 修改姓名 + 邮箱/手机列表更新后的页面
3. `03_appointment_create.png` —— 创建预约成功提示
4. `04_appointment_cancel.png` —— 取消预约并显示 status=CANCELLED + reason
5. `05_challenge_create_join.png` —— 创建挑战 + 用户加入后的列表
6. `06_challenge_invite.png` —— 发送 EMAIL/PHONE 邀请的结果
7. `07_summary_metrics.png` —— 月度指标统计或预约计数结果
8. `08_search_records.png` —— 健康记录搜索结果（按 health_id/metric）
9. `09_provider_portal.png` —— Provider 登录后预约列表
10. `10_family_view.png` —— 家庭成员记录/挑战查看

## 截图提示
- 页面位置：`frontend/login.html` → 用户主菜单 `index.html` 或 `provider.html`
- 数据选择：使用 seed 用户 `userId=1`、Provider `LIC-10001`，便于复现
- 状态覆盖：展示成功与错误（如 24 小时内取消失败）可选 2 张对照
- 清晰度：尽量包含页面标题和关键结果区域；若弹窗/提示请一并截取
