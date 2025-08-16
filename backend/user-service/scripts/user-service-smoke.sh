#!/usr/bin/env bash
set -euo pipefail

# Simple multi-tenant smoke for user-service.
# Requirements: curl; jq or python3 (for JSON parsing).
# Usage examples:
#  ./user-service-smoke.sh http://localhost:70 acme.example.com other.example.com
#  ./user-service-smoke.sh https://user.example.com acme.domain.com other.domain.com

BASE_URL=${1:-http://localhost:70}
HOST_ACME=${2:-acme.example.com}
HOST_OTHER=${3:-other.example.com}

JSON_GET() {
  local key=$1
  if command -v jq >/dev/null 2>&1; then
    jq -r ".${key}"
  elif command -v python3 >/dev/null 2>&1; then
    python3 -c "import sys,json;print(json.load(sys.stdin).get('${key}',''))"
  else
    echo "Missing jq/python3 for JSON parsing" >&2
    exit 1
  fi
}

echo "Base URL: ${BASE_URL}" && echo "ACME Host: ${HOST_ACME}" && echo "OTHER Host: ${HOST_OTHER}" && echo
TS=$(date +%s)
USER_EMAIL="user_${TS}@test.com"
ADMIN_EMAIL="admin_${TS}@acme.com"
PASSWORD="Password1!"

echo "1) Register USER on ACME (tenant=acme)"
curl -sS -D /tmp/resp_headers.txt -o /tmp/reg_user.json \
  -H "Host: ${HOST_ACME}" -H 'Content-Type: application/json' \
  -X POST "${BASE_URL}/api/auth/register" \
  --data "{\"name\":\"User A\",\"email\":\"${USER_EMAIL}\",\"password\":\"${PASSWORD}\",\"role\":\"USER\"}"
REG_STATUS=$(head -n1 /tmp/resp_headers.txt | awk '{print $2}')
echo "   Status: ${REG_STATUS}"; test "${REG_STATUS}" = "201" || { echo "Registration failed"; exit 1; }

echo "2) Login USER on ACME"
curl -sS -D /tmp/resp_headers.txt -o /tmp/login_user.json \
  -H "Host: ${HOST_ACME}" -H 'Content-Type: application/json' \
  -X POST "${BASE_URL}/api/auth/login" \
  --data "{\"email\":\"${USER_EMAIL}\",\"password\":\"${PASSWORD}\"}"
LOGIN_STATUS=$(head -n1 /tmp/resp_headers.txt | awk '{print $2}')
ACCESS_TOKEN=$(cat /tmp/login_user.json | JSON_GET accessToken)
REFRESH_TOKEN=$(cat /tmp/login_user.json | JSON_GET refreshToken)
echo "   Status: ${LOGIN_STATUS}"; test "${LOGIN_STATUS}" = "200" || { echo "Login failed"; exit 1; }

echo "3) GET /users/me (ACME) should be 200"
curl -sS -D /tmp/resp_headers.txt -o /dev/null \
  -H "Host: ${HOST_ACME}" -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  "${BASE_URL}/api/users/me"
ME_STATUS=$(head -n1 /tmp/resp_headers.txt | awk '{print $2}')
echo "   Status: ${ME_STATUS}"; test "${ME_STATUS}" = "200" || { echo "Profile failed"; exit 1; }

echo "4) Cross-tenant GET /users/me (OTHER) should be 403 when enforcement on"
curl -sS -D /tmp/resp_headers.txt -o /dev/null \
  -H "Host: ${HOST_OTHER}" -H "Authorization: Bearer ${ACCESS_TOKEN}" \
  "${BASE_URL}/api/users/me"
ME_X_STATUS=$(head -n1 /tmp/resp_headers.txt | awk '{print $2}')
echo "   Status: ${ME_X_STATUS}"; if [ "${ME_X_STATUS}" != "403" ]; then echo "WARN: expected 403, got ${ME_X_STATUS}"; fi

# Admin path
echo "5) Register ADMIN on ACME"
curl -sS -D /tmp/resp_headers.txt -o /tmp/reg_admin.json \
  -H "Host: ${HOST_ACME}" -H 'Content-Type: application/json' \
  -X POST "${BASE_URL}/api/auth/register" \
  --data "{\"name\":\"Admin A\",\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${PASSWORD}\",\"role\":\"ADMIN\"}"
echo "   Status: $(head -n1 /tmp/resp_headers.txt | awk '{print $2}')"

echo "6) Login ADMIN on ACME"
curl -sS -D /tmp/resp_headers.txt -o /tmp/login_admin.json \
  -H "Host: ${HOST_ACME}" -H 'Content-Type: application/json' \
  -X POST "${BASE_URL}/api/auth/login" \
  --data "{\"email\":\"${ADMIN_EMAIL}\",\"password\":\"${PASSWORD}\"}"
ADMIN_ACCESS=$(cat /tmp/login_admin.json | JSON_GET accessToken)
echo "   Status: $(head -n1 /tmp/resp_headers.txt | awk '{print $2}')"

echo "7) Admin list users on ACME (expect 200, includes ACME users only)"
curl -sS -D /tmp/resp_headers.txt -o /dev/null \
  -H "Host: ${HOST_ACME}" -H "Authorization: Bearer ${ADMIN_ACCESS}" \
  "${BASE_URL}/api/admin/users"
ADMIN_LIST_STATUS=$(head -n1 /tmp/resp_headers.txt | awk '{print $2}')
echo "   Status: ${ADMIN_LIST_STATUS}"; test "${ADMIN_LIST_STATUS}" = "200" || { echo "Admin list failed"; exit 1; }

echo "8) Cross-tenant admin list on OTHER (expect 403)"
curl -sS -D /tmp/resp_headers.txt -o /dev/null \
  -H "Host: ${HOST_OTHER}" -H "Authorization: Bearer ${ADMIN_ACCESS}" \
  "${BASE_URL}/api/admin/users"
ADMIN_LIST_X_STATUS=$(head -n1 /tmp/resp_headers.txt | awk '{print $2}')
echo "   Status: ${ADMIN_LIST_X_STATUS}"; if [ "${ADMIN_LIST_X_STATUS}" != "403" ]; then echo "WARN: expected 403, got ${ADMIN_LIST_X_STATUS}"; fi

# Refresh flow
echo "9) Refresh token on ACME and reuse access"
curl -sS -D /tmp/resp_headers.txt -o /tmp/refresh.json \
  -H "Host: ${HOST_ACME}" -H 'Content-Type: application/json' \
  -X POST "${BASE_URL}/api/auth/refresh" \
  --data "{\"refreshToken\":\"${REFRESH_TOKEN}\"}"
REFRESH_STATUS=$(head -n1 /tmp/resp_headers.txt | awk '{print $2}')
NEW_ACCESS=$(cat /tmp/refresh.json | JSON_GET accessToken)
echo "   Status: ${REFRESH_STATUS}"; test "${REFRESH_STATUS}" = "200" || { echo "Refresh failed"; exit 1; }

echo "10) Use new access on ACME (200) and OTHER (403 expected)"
curl -sS -D /tmp/resp_headers.txt -o /dev/null \
  -H "Host: ${HOST_ACME}" -H "Authorization: Bearer ${NEW_ACCESS}" \
  "${BASE_URL}/api/users/me"; echo "   ACME: $(head -n1 /tmp/resp_headers.txt | awk '{print $2}')"

curl -sS -D /tmp/resp_headers.txt -o /dev/null \
  -H "Host: ${HOST_OTHER}" -H "Authorization: Bearer ${NEW_ACCESS}" \
  "${BASE_URL}/api/users/me"; echo "   OTHER: $(head -n1 /tmp/resp_headers.txt | awk '{print $2}')"

echo
echo "Smoke complete. Review WARN lines if any status differed from expectations."
