## HealthTrack Phase 3 Demo Checklist (Screenshots)

Use seed users (userId 1..6) or create a new user from the main menu.

### 1) Main Menu
- Open `frontend/index.html`
- Select an existing userId (e.g., 1) and confirm the “Current userId” pill updates

### 2) Account Info
- Modify personal details (full name) and refresh to show saved result
- Add email address and refresh to show it in the list
- Remove an email address and refresh
- Add/Update phone number and refresh
- Remove phone number and refresh
- Link a provider and refresh to show it in linked providers
- Set primary care provider and refresh
- Unlink a provider and refresh

### 3) Book Appointment
- Create appointment with provider + scheduled time + type + memo
- Search appointments and show the new appointment
- Cancel a scheduled appointment (enter reason) and show status=CANCELLED + reason stored
  - (Note: cancelling is allowed only if scheduled more than 24 hours in the future)

### 4) Wellness Challenge
- Create a new challenge and refresh list
- Select a challenge and Join
- Load participants list
- Update progress for a participant; mark completed
- Send invitations:
  - EMAIL invite to a “new user” email
  - PHONE invite to a “new user” phone
- Show invitations list (pending/expired/accepted can be demonstrated via seed data too)

### 5) Monthly Summary
- Appointment count for a date range
- Metric stats avg/min/max for a month (weight/steps/blood pressure)
- Top challenges by participant count
- Most active users list

### 6) Search Records
- Appointment search with filters (provider/type/date range)
- Health record search with filters (metric/date range)

### 7) Sign Out
- Use “Sign Out” on main menu (clears selected user)


