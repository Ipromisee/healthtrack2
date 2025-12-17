-- HealthTrack Phase 3: Seed data
USE healthtrack;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE invitation;
TRUNCATE TABLE challenge_participant;
TRUNCATE TABLE challenge;
TRUNCATE TABLE appointment_cancellation;
TRUNCATE TABLE appointment;
TRUNCATE TABLE health_record;
TRUNCATE TABLE metric_type;
TRUNCATE TABLE family_group_member;
TRUNCATE TABLE family_group;
TRUNCATE TABLE user_provider_link;
TRUNCATE TABLE user_email;
TRUNCATE TABLE user_phone;
TRUNCATE TABLE user_account;
TRUNCATE TABLE provider_account;
TRUNCATE TABLE provider;
SET FOREIGN_KEY_CHECKS = 1;

-- Providers
INSERT INTO provider (id, license_no, display_name, specialty, email, phone, is_verified) VALUES
  (1, 'LIC-10001', 'Dr. Alice Chen', 'Primary Care', 'alice.chen@clinic.example', '555-4001', TRUE),
  (2, 'LIC-10002', 'Dr. Brian Kim', 'Dermatology', 'brian.kim@clinic.example', '555-4002', TRUE),
  (3, 'LIC-10003', 'Dr. Carla Singh', 'Therapy', 'carla.singh@clinic.example', '555-4003', FALSE),
  (4, 'LIC-10004', 'Dr. David Park', 'Cardiology', 'david.park@clinic.example', '555-4004', TRUE);

-- Provider accounts (login_code is a simple demo password)
-- Example: provider license_no=LIC-10001, login_code=provider123
INSERT INTO provider_account (provider_id, login_code) VALUES
  (1, 'provider123'),
  (2, 'provider123'),
  (3, 'provider123'),
  (4, 'provider123');

-- Users
INSERT INTO user_account (id, health_id, full_name, primary_provider_id) VALUES
  (1, 'H-0001', 'Xiao YiWei', 1),
  (2, 'H-0002', 'Liam Zhang', 1),
  (3, 'H-0003', 'Mia Chen', 2),
  (4, 'H-0004', 'Noah Wang', 4),
  (5, 'H-0005', 'Emma Liu', NULL),
  (6, 'H-0006', 'Olivia Sun', 3);

-- Phones (one per user)
INSERT INTO user_phone (user_id, phone, is_verified, verified_at) VALUES
  (1, '555-1001', TRUE,  NOW() - INTERVAL 200 DAY),
  (2, '555-1002', TRUE,  NOW() - INTERVAL 180 DAY),
  (3, '555-1003', FALSE, NULL),
  (4, '555-1004', TRUE,  NOW() - INTERVAL 90 DAY),
  (5, '555-1005', FALSE, NULL),
  (6, '555-1006', TRUE,  NOW() - INTERVAL 30 DAY);

-- Emails (multiple per user)
INSERT INTO user_email (user_id, email, is_verified, verified_at) VALUES
  (1, 'xiaoyiwei@example.com', TRUE,  NOW() - INTERVAL 200 DAY),
  (1, 'xiaoyialt@example.com', FALSE, NULL),
  (2, 'liam.zhang@example.com', TRUE, NOW() - INTERVAL 180 DAY),
  (2, 'liam.work@example.com', TRUE, NOW() - INTERVAL 100 DAY),
  (3, 'mia.chen@example.com', FALSE, NULL),
  (4, 'noah.wang@example.com', TRUE, NOW() - INTERVAL 90 DAY),
  (5, 'emma.liu@example.com', TRUE, NOW() - INTERVAL 60 DAY),
  (6, 'olivia.sun@example.com', TRUE, NOW() - INTERVAL 30 DAY);

-- Link providers (multi-provider per user)
INSERT INTO user_provider_link (user_id, provider_id) VALUES
  (1, 1), (1, 2),
  (2, 1),
  (3, 2), (3, 4),
  (4, 4),
  (5, 1),
  (6, 3);

-- Family groups (2+ users)
INSERT INTO family_group (id, group_name) VALUES
  (1, 'Wei Family'),
  (2, 'Roommates');

INSERT INTO family_group_member (family_group_id, user_id, member_role, can_manage) VALUES
  (1, 1, 'OWNER', TRUE),
  (1, 5, 'MEMBER', FALSE),
  (2, 2, 'OWNER', TRUE),
  (2, 4, 'MEMBER', FALSE);

-- Metric types
INSERT INTO metric_type (id, code, display_name, unit) VALUES
  (1, 'weight', '体重', 'kg'),
  (2, 'bp_systolic', '收缩压', 'mmHg'),
  (3, 'bp_diastolic', '舒张压', 'mmHg'),
  (4, 'steps', '步数', '步');

-- Health records across multiple months (for monthly summaries and active users)
-- User 1: frequent logs
INSERT INTO health_record (user_id, metric_type_id, recorded_at, metric_value, note) VALUES
  (1, 1, NOW() - INTERVAL 70 DAY, 62.1, 'morning'),
  (1, 1, NOW() - INTERVAL 40 DAY, 61.7, 'morning'),
  (1, 1, NOW() - INTERVAL 10 DAY, 61.2, 'morning'),
  (1, 2, NOW() - INTERVAL 70 DAY, 118, NULL),
  (1, 3, NOW() - INTERVAL 70 DAY, 78, NULL),
  (1, 2, NOW() - INTERVAL 10 DAY, 121, NULL),
  (1, 3, NOW() - INTERVAL 10 DAY, 80, NULL),
  (1, 4, NOW() - INTERVAL 25 DAY, 10500, NULL),
  (1, 4, NOW() - INTERVAL 5 DAY, 12000, 'walked more');

-- User 2: steps-heavy logs
INSERT INTO health_record (user_id, metric_type_id, recorded_at, metric_value) VALUES
  (2, 4, NOW() - INTERVAL 28 DAY, 8000),
  (2, 4, NOW() - INTERVAL 27 DAY, 9500),
  (2, 4, NOW() - INTERVAL 26 DAY, 11000),
  (2, 4, NOW() - INTERVAL 6 DAY, 14000);

-- User 3: few logs
INSERT INTO health_record (user_id, metric_type_id, recorded_at, metric_value) VALUES
  (3, 1, NOW() - INTERVAL 35 DAY, 55.2),
  (3, 2, NOW() - INTERVAL 35 DAY, 115),
  (3, 3, NOW() - INTERVAL 35 DAY, 75);

-- Appointments (some cancelled)
INSERT INTO appointment (id, user_id, provider_id, scheduled_at, appointment_type, memo, status) VALUES
  (1001, 1, 1, NOW() + INTERVAL 10 DAY, 'IN_PERSON', 'Annual checkup', 'SCHEDULED'),
  (1002, 1, 2, NOW() + INTERVAL 3 DAY,  'VIRTUAL',   'Skin rash questions', 'SCHEDULED'),
  (1003, 2, 1, NOW() + INTERVAL 12 DAY, 'VIRTUAL',   'Follow-up', 'SCHEDULED'),
  (1004, 3, 2, NOW() - INTERVAL 20 DAY, 'IN_PERSON', 'Completed visit', 'COMPLETED'),
  (1005, 4, 4, NOW() + INTERVAL 2 DAY,  'IN_PERSON', 'Chest discomfort', 'CANCELLED');

INSERT INTO appointment_cancellation (appointment_id, cancelled_at, cancel_reason) VALUES
  (1005, NOW() - INTERVAL 1 HOUR, 'Patient Rescheduled');

-- Challenges
INSERT INTO challenge (id, creator_user_id, goal_text, start_date, end_date) VALUES
  (2001, 1, 'Walk 100 miles in a month', DATE(NOW() - INTERVAL 20 DAY), DATE(NOW() + INTERVAL 10 DAY)),
  (2002, 2, 'Log weight weekly for 2 months', DATE(NOW() - INTERVAL 40 DAY), DATE(NOW() + INTERVAL 20 DAY)),
  (2003, 4, 'Meditate 10 minutes daily for 30 days', DATE(NOW() - INTERVAL 5 DAY), DATE(NOW() + INTERVAL 25 DAY));

-- Challenge participants + progress (for "most participants" and "most active users")
INSERT INTO challenge_participant (challenge_id, user_id, progress_value, is_completed, completed_at) VALUES
  (2001, 1, 55.0, FALSE, NULL),
  (2001, 2, 80.0, FALSE, NULL),
  (2001, 5, 20.0, FALSE, NULL),
  (2002, 2, 6.0,  TRUE,  NOW() - INTERVAL 2 DAY),
  (2002, 3, 4.0,  FALSE, NULL),
  (2003, 4, 5.0,  FALSE, NULL),
  (2003, 1, 3.0,  FALSE, NULL);

-- Invitations (pending/accepted/expired)
-- Pending invite to an unverified email/phone (new user scenario)
INSERT INTO invitation
  (challenge_id, sender_user_id, recipient_type, recipient_value, recipient_user_id, status, initiated_at, completed_at, expires_at)
VALUES
  (2001, 1, 'EMAIL', 'new.friend@example.com', NULL, 'PENDING',  NOW() - INTERVAL 2 DAY, NULL, NOW() + INTERVAL 13 DAY),
  (2001, 1, 'PHONE', '555-9999',               NULL, 'PENDING',  NOW() - INTERVAL 1 DAY, NULL, NOW() + INTERVAL 14 DAY);

-- Accepted invite (recipient already a user)
INSERT INTO invitation
  (challenge_id, sender_user_id, recipient_type, recipient_value, recipient_user_id, status, initiated_at, completed_at, expires_at)
VALUES
  (2002, 2, 'EMAIL', 'mia.chen@example.com', 3, 'ACCEPTED', NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 9 DAY, NOW() + INTERVAL 5 DAY);

-- Expired invite (older than 15 days)
INSERT INTO invitation
  (challenge_id, sender_user_id, recipient_type, recipient_value, recipient_user_id, status, initiated_at, completed_at, expires_at)
VALUES
  (2003, 4, 'EMAIL', 'expired.user@example.com', NULL, 'EXPIRED', NOW() - INTERVAL 20 DAY, NULL, NOW() - INTERVAL 5 DAY);


