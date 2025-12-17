## Phase 3 Deliverable — HealthTrack Personal Wellness Platform

### Goal of Phase 3
Phase 3 implements the **HealthTrack Personal Wellness Platform** database and an application program with a GUI. The goals are:
- Create the database schema (tables with PK/FK/secondary keys and constraints).
- Create a database instance (populate each table with sufficient tuples, without integrity violations).
- Implement the required application menus and functions (GUI + database queries).

### Revisions from Phase 2
Phase 1/2 artifacts were not used as an input. In this phase, the relational schema was derived directly from the given specifications in `docs/project_description.md`, and the assumptions below were stated explicitly to avoid contradicting the specification.

### Assumptions (do not contradict the specification)
- `health_id` is globally unique per user account.
- Email addresses and phone numbers are treated as globally unique identifiers across the system (helps support “invitation to a new user”).
- A user can link multiple providers; **primary care physician** is represented as `user_account.primary_provider_id` (at most one).
- The verification process itself is out of scope, but the database records `is_verified` flags for contact information and providers.
- Appointment cancellation rule (24 hours) is enforced at the **application layer** (API) as required by the spec.
- “Most active users” is computed as: number of `health_record` rows + number of completed challenge participations (`challenge_participant.is_completed = TRUE`).

---

## Database Schema Creation

### Schema SQL
- **File**: `sql/01_schema.sql`
- **DBMS**: MySQL 8.x
- Includes:
  - Primary keys (PK)
  - Foreign keys (FK) with appropriate ON DELETE / ON UPDATE actions
  - Unique constraints (“secondary keys”)
  - Indexes needed for search and monthly summary queries

### Main tables and how they satisfy requirements
- **Users/Accounts**
  - `user_account`: stores `health_id` and `full_name`, plus optional `primary_provider_id`
  - `user_email`: multiple emails per user, with `is_verified`
  - `user_phone`: exactly one phone row per user (PK = user_id), with `is_verified`
- **Healthcare Providers**
  - `provider`: unique `license_no`, verification flag
  - `user_provider_link`: many-to-many link between users and providers
- **Family Groups**
  - `family_group`, `family_group_member`: group membership and permissions/role fields
- **Appointments**
  - `appointment`: unique appointment id, type, time, memo, status
  - `appointment_cancellation`: cancellation reason/time (exists only if cancelled)
- **Wellness Challenges & Invitations**
  - `challenge`: unique challenge id, goal text, start/end dates
  - `challenge_participant`: participant set + progress + completion
  - `invitation`: recipient (email/phone), initiated/completed/expired timestamps, 15-day expiry
- **Health Metrics for monthly summary**
  - `metric_type`: metric catalog (weight, bp, steps…)
  - `health_record`: recorded metric values with time for monthly stats

---

## Database Instance Creation (Population)

### Seed SQL
- **File**: `sql/02_seed.sql`
- Populates all tables with sufficient tuples, including:
  - Multiple users with multiple emails
  - Verified and non-verified contacts/providers
  - Linked providers + primary care set for some users
  - Family groups with 2+ members
  - Appointments including scheduled, completed, and cancelled (with cancellation reason)
  - Challenges with multiple participants and different progress states
  - Invitations in PENDING, ACCEPTED, and EXPIRED states
  - Health records across multiple months for monthly summary and “most active users”

---

## Application Programs (GUI + APIs)

### Implementation approach
- **Backend**: Java + Spring Boot (REST API) + MySQL, using JDBC (`JdbcTemplate`) for SQL queries.
- **Frontend**: HTML + CSS + JavaScript static pages calling the REST API with `fetch`.

### How to run
1. **Create schema** in MySQL:
   - Run `sql/01_schema.sql`
2. **Insert seed data**:
   - Run `sql/02_seed.sql`
3. **Configure backend DB password**:
   - Edit `backend/src/main/resources/application.properties` and set `spring.datasource.password`
4. **Start backend**:
   - From `backend/`: `mvn spring-boot:run`
5. **Open GUI**:
   - Open `frontend/index.html` in a browser

### Required menus and functions (Deliverable 3)
Main menu (implemented by pages in `frontend/pages/`):
- **Account Info** (`account.html`)
  - Modify personal details
  - Add/remove email address
  - Add/remove phone number
  - Add/remove/link a healthcare provider, set/clear primary care provider
- **Book an Appointment** (`appointment.html`)
  - Create appointment with provider + time + type + memo
  - Cancel appointment with recorded cancellation reason
  - 24-hour cancellation rule enforced by API
- **Create a Wellness Challenge** (`challenge.html`)
  - Create challenge (goal + start/end)
  - Join challenge, update participant progress, mark complete
  - Send invitations to email/phone (new user if unknown), 15-day expiry stored
- **Monthly Health Summary** (`summary.html`)
  - Total number of appointments for a user in a date range
  - Average/min/max of a metric per month
  - Challenges with most participants
  - Most active users
- **Search Records** (`search.html`)
  - Search appointments by health_id + provider + type + date range
  - Search health records by health_id + metric + date range
- **Sign Out**
  - Implemented by clearing the selected user in browser local storage

---

## Problems Encountered & Solutions
- **Primary care physician constraint**: MySQL does not support partial unique indexes (only one “primary” row) easily on a link table. Solution: model primary care as a nullable FK in `user_account.primary_provider_id`.
- **24-hour cancellation rule**: Implemented at the application layer by comparing `scheduled_at - 24h` with current time before cancellation.
- **Invitation to new user & uniqueness**: Invitations use recipient email/phone as an identifier. Solution: treat email/phone as globally unique and store recipient as `recipient_type` + `recipient_value` with a uniqueness constraint per challenge.

---

## Screenshots / Program Demonstration Checklist
Capture screenshots showing each required function:
1. Main menu + selecting a user (seed userIds 1..6) or creating a new user
2. Account Info: update name, add email, remove email, add/update phone, remove phone
3. Account Info: link provider, set primary care, unlink provider
4. Book appointment: create appointment (show new row)
5. Cancel appointment: cancel a scheduled appointment and show stored cancellation reason
6. Create challenge: create a challenge and show it in list
7. Join challenge: join as user and show participant list
8. Update progress and mark completed
9. Invite: send invite to an email/phone; show invitations table (pending/accepted/expired examples from seed data)
10. Monthly summary: appointment count, metric avg/min/max, top active users
11. Search: appointment search filters; health record search filters


