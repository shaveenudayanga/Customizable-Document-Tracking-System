# User Service — Multi-tenant Smoke Guide

## Local (docker compose)

1) Prepare `.env` in `backend/user-service`:

```
SPRING_PROFILES_ACTIVE=prod
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
JWT_SECRET=dev-long-random-secret
TENANT_ENFORCE=true
TENANT_LOGIN_ENFORCE=false
```

2) Start stack (from `backend/user-service`):

```
docker compose up -d --build
```

3) Run scripted smoke (ACME vs OTHER):

```
./scripts/user-service-smoke.sh http://localhost:70 acme.example.com other.example.com
```

4) Manual curl (optional):

```
# Register under acme
curl -H 'Host: acme.example.com' -H 'Content-Type: application/json' \
  -d '{"name":"User","email":"u@test.com","password":"Password1!","role":"USER"}' \
  http://localhost:70/api/auth/register -i

# Login
ACCESS=$(curl -s -H 'Host: acme.example.com' -H 'Content-Type: application/json' \
  -d '{"email":"u@test.com","password":"Password1!"}' \
  http://localhost:70/api/auth/login | jq -r .accessToken)

# Same-tenant profile (200)
curl -H 'Host: acme.example.com' -H "Authorization: Bearer ${ACCESS}" \
  http://localhost:70/api/users/me -i

# Cross-tenant profile (403)
curl -H 'Host: other.example.com' -H "Authorization: Bearer ${ACCESS}" \
  http://localhost:70/api/users/me -i
```

## Postman

- Import `postman_collection.json` and these environments:
  - `postman_environment.json` (acme.example.com)
  - `postman_environment_other.json` (other.example.com)
- Use `baseUrl` http://localhost:70, switch `tenantHost` to test cross-tenant behavior.

## k8s staging

- Ensure ingress wildcard subdomains route to user-service.
- Set env flags in Deployment:
  - `TENANT_ENFORCE=true`
  - `TENANT_LOGIN_ENFORCE=false` (enable later to test 401 on cross-tenant login)
- Run the same smoke script with your ingress base URL.

## Expected results
- Same-tenant login and /users/me → 200
- Cross-tenant /users/me → 403 (with enforcement)
- With login enforcement on, cross-tenant login → 401
- Admin list scoped to tenant; cross-tenant admin call → 403
- Refresh returns new access; cross-tenant use blocked
