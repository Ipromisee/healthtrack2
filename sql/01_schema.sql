-- HealthTrack Personal Wellness Platform
-- Phase 3: Schema (MySQL 8.x)
--
-- Notes / assumptions (also echoed in the Phase 3 report):
-- 1) `health_id` is globally unique per user account.
-- 2) Emails and phone numbers are treated as globally unique identifiers (to support invitations to "new users").
-- 3) "Primary care physician" is modeled as `user_account.primary_provider_id` (at most one).
-- 4) Appointment cancellation details are stored only when an appointment is cancelled.

CREATE DATABASE IF NOT EXISTS healthtrack
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_general_ci;

USE healthtrack;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS appointment_cancellation;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS invitation;
DROP TABLE IF EXISTS challenge_participant;
DROP TABLE IF EXISTS challenge;
DROP TABLE IF EXISTS health_record;
DROP TABLE IF EXISTS metric_type;
DROP TABLE IF EXISTS family_group_member;
DROP TABLE IF EXISTS family_group;
DROP TABLE IF EXISTS user_provider_link;
DROP TABLE IF EXISTS user_email;
DROP TABLE IF EXISTS user_phone;
DROP TABLE IF EXISTS user_account;
DROP TABLE IF EXISTS provider_account;
DROP TABLE IF EXISTS provider;

SET FOREIGN_KEY_CHECKS = 1;

-- Providers (doctors, specialists, therapists)
CREATE TABLE provider (
  id BIGINT NOT NULL AUTO_INCREMENT,
  license_no VARCHAR(64) NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  specialty VARCHAR(120) NULL,
  email VARCHAR(255) NULL,
  phone VARCHAR(20) NULL,
  is_verified BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_provider_license_no (license_no),
  UNIQUE KEY uk_provider_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Provider login credentials (simple demo authentication)
CREATE TABLE provider_account (
  provider_id BIGINT NOT NULL,
  login_code VARCHAR(64) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (provider_id),
  CONSTRAINT fk_provider_account_provider
    FOREIGN KEY (provider_id) REFERENCES provider(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- User accounts
CREATE TABLE user_account (
  id BIGINT NOT NULL AUTO_INCREMENT,
  health_id VARCHAR(32) NOT NULL,
  full_name VARCHAR(120) NOT NULL,
  primary_provider_id BIGINT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_health_id (health_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- One phone number per account
CREATE TABLE user_phone (
  user_id BIGINT NOT NULL,
  phone VARCHAR(20) NOT NULL,
  is_verified BOOLEAN NOT NULL DEFAULT FALSE,
  verified_at DATETIME NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id),
  UNIQUE KEY uk_user_phone_phone (phone),
  CONSTRAINT fk_user_phone_user
    FOREIGN KEY (user_id) REFERENCES user_account(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Multiple emails per account
CREATE TABLE user_email (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  email VARCHAR(255) NOT NULL,
  is_verified BOOLEAN NOT NULL DEFAULT FALSE,
  verified_at DATETIME NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_email_email (email),
  KEY idx_user_email_user_id (user_id),
  CONSTRAINT fk_user_email_user
    FOREIGN KEY (user_id) REFERENCES user_account(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Users can link multiple providers; provider itself can be verified or not.
CREATE TABLE user_provider_link (
  user_id BIGINT NOT NULL,
  provider_id BIGINT NOT NULL,
  linked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, provider_id),
  CONSTRAINT fk_user_provider_user
    FOREIGN KEY (user_id) REFERENCES user_account(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_user_provider_provider
    FOREIGN KEY (provider_id) REFERENCES provider(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Family groups
CREATE TABLE family_group (
  id BIGINT NOT NULL AUTO_INCREMENT,
  group_name VARCHAR(120) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE family_group_member (
  family_group_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  member_role ENUM('OWNER','MEMBER','MANAGER') NOT NULL DEFAULT 'MEMBER',
  can_manage BOOLEAN NOT NULL DEFAULT FALSE,
  joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (family_group_id, user_id),
  KEY idx_family_member_user_id (user_id),
  CONSTRAINT fk_family_member_group
    FOREIGN KEY (family_group_id) REFERENCES family_group(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_family_member_user
    FOREIGN KEY (user_id) REFERENCES user_account(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Health metric types (weight, bp_systolic, steps, ...)
CREATE TABLE metric_type (
  id BIGINT NOT NULL AUTO_INCREMENT,
  code VARCHAR(64) NOT NULL,
  display_name VARCHAR(120) NOT NULL,
  unit VARCHAR(32) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_metric_type_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Health records used for monthly summary / analytics
CREATE TABLE health_record (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  metric_type_id BIGINT NOT NULL,
  recorded_at DATETIME NOT NULL,
  metric_value DECIMAL(12,3) NOT NULL,
  note VARCHAR(255) NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_health_record_user_time (user_id, recorded_at),
  KEY idx_health_record_metric_time (metric_type_id, recorded_at),
  CONSTRAINT fk_health_record_user
    FOREIGN KEY (user_id) REFERENCES user_account(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_health_record_metric
    FOREIGN KEY (metric_type_id) REFERENCES metric_type(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Appointments
CREATE TABLE appointment (
  id BIGINT NOT NULL AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  provider_id BIGINT NOT NULL,
  scheduled_at DATETIME NOT NULL,
  appointment_type ENUM('IN_PERSON','VIRTUAL') NOT NULL,
  memo TEXT NULL,
  status ENUM('SCHEDULED','CANCELLED','COMPLETED') NOT NULL DEFAULT 'SCHEDULED',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_appointment_user_time (user_id, scheduled_at),
  KEY idx_appointment_provider_time (provider_id, scheduled_at),
  CONSTRAINT fk_appointment_user
    FOREIGN KEY (user_id) REFERENCES user_account(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_appointment_provider
    FOREIGN KEY (provider_id) REFERENCES provider(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Cancellation details (only when cancelled)
CREATE TABLE appointment_cancellation (
  appointment_id BIGINT NOT NULL,
  cancelled_at DATETIME NOT NULL,
  cancel_reason VARCHAR(255) NOT NULL,
  PRIMARY KEY (appointment_id),
  CONSTRAINT fk_cancel_appointment
    FOREIGN KEY (appointment_id) REFERENCES appointment(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Wellness challenges
CREATE TABLE challenge (
  id BIGINT NOT NULL AUTO_INCREMENT,
  creator_user_id BIGINT NOT NULL,
  goal_text VARCHAR(255) NOT NULL,
  start_date DATE NOT NULL,
  end_date DATE NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_challenge_creator (creator_user_id),
  CONSTRAINT fk_challenge_creator
    FOREIGN KEY (creator_user_id) REFERENCES user_account(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Participants and progress
CREATE TABLE challenge_participant (
  challenge_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  joined_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  progress_value DECIMAL(12,3) NOT NULL DEFAULT 0,
  is_completed BOOLEAN NOT NULL DEFAULT FALSE,
  completed_at DATETIME NULL,
  PRIMARY KEY (challenge_id, user_id),
  KEY idx_challenge_participant_user (user_id),
  CONSTRAINT fk_challenge_participant_challenge
    FOREIGN KEY (challenge_id) REFERENCES challenge(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_challenge_participant_user
    FOREIGN KEY (user_id) REFERENCES user_account(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Invitations for challenges (email/phone), can later be linked to a user upon signup
CREATE TABLE invitation (
  id BIGINT NOT NULL AUTO_INCREMENT,
  challenge_id BIGINT NOT NULL,
  sender_user_id BIGINT NOT NULL,
  recipient_type ENUM('EMAIL','PHONE') NOT NULL,
  recipient_value VARCHAR(255) NOT NULL,
  recipient_user_id BIGINT NULL,
  status ENUM('PENDING','ACCEPTED','EXPIRED','CANCELLED') NOT NULL DEFAULT 'PENDING',
  initiated_at DATETIME NOT NULL,
  completed_at DATETIME NULL,
  expires_at DATETIME NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_invitation_unique_recipient (challenge_id, recipient_type, recipient_value),
  KEY idx_invitation_recipient_user (recipient_user_id),
  CONSTRAINT fk_invitation_challenge
    FOREIGN KEY (challenge_id) REFERENCES challenge(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_invitation_sender
    FOREIGN KEY (sender_user_id) REFERENCES user_account(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,
  CONSTRAINT fk_invitation_recipient_user
    FOREIGN KEY (recipient_user_id) REFERENCES user_account(id)
    ON UPDATE CASCADE
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Add deferred foreign keys (helps pinpoint exact FK failures)
ALTER TABLE user_account
  ADD CONSTRAINT fk_user_primary_provider
  FOREIGN KEY (primary_provider_id) REFERENCES provider(id)
  ON UPDATE CASCADE
  ON DELETE SET NULL;


