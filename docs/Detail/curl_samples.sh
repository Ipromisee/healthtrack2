#!/usr/bin/env bash
# Quick curl samples for core flows (adjust IDs/timestamps as needed)

API=${API:-http://localhost:8080/api}

set -e

echo "List providers"
curl -s "$API/account/providers" | jq .

echo "Create appointment (user 1 with provider 1)"
curl -s -X POST "$API/appointment/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"providerId":1,"scheduledAt":"2025-01-20T10:00:00","appointmentType":"IN_PERSON","memo":"demo"}' | jq .

echo "Search appointments for H-0001"
curl -s -X POST "$API/appointment/search" \
  -H "Content-Type: application/json" \
  -d '{"healthId":"H-0001"}' | jq .

echo "Log a health record (weight)"
curl -s -X POST "$API/health-record/create" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"metricCode":"weight","metricValue":61.5,"recordedAt":"2025-01-18T08:00:00","note":"morning"}' | jq .

echo "Create challenge and invite by email"
CID=$(curl -s -X POST "$API/challenge/create" \
  -H "Content-Type: application/json" \
  -d '{"creatorUserId":1,"goalText":"10k steps for 7 days","startDate":"2025-01-15","endDate":"2025-01-22"}')
curl -s -X POST "$API/challenge/invite" \
  -H "Content-Type: application/json" \
  -d "{\"challengeId\":$CID,\"senderUserId\":1,\"recipientType\":\"EMAIL\",\"recipientValue\":\"friend@example.com\"}" | jq .

echo "Monthly metric stats (steps)"
curl -s -X POST "$API/summary/metric-stats" \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"metricCode":"steps","monthStart":"2025-01-01","monthEnd":"2025-01-31"}' | jq .

echo "Provider login and list appointments"
curl -s -X POST "$API/provider/login" \
  -H "Content-Type: application/json" \
  -d '{"licenseNo":"LIC-10001","loginCode":"provider123"}' | jq .
curl -s "$API/provider/1/appointments" | jq .
