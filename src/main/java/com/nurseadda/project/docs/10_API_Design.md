# API Design

## Authentication

POST /api/auth/register/staff   (fullName, email, phone, staffCategory, password)

POST /api/auth/register/client  (email, password, phone)

POST /api/auth/login

POST /api/auth/logout

## Email OTP verification

Every newly registered account (staff or client) starts UNVERIFIED. The
register endpoints auto-send a 6-digit OTP to the user's email and return
no tokens (`accessToken`/`refreshToken` are null, `emailVerified` is
false). The user must prove ownership of the email before the account is
usable:

POST /api/auth/otp/verify   (email, otp) -> marks emailVerified=true, returns
                            fresh access + refresh tokens

POST /api/auth/otp/resend   (email) -> generates a new OTP and emails it
                            (use when the original expired or never arrived)

Login is BLOCKED (401 "Please verify your email address using the OTP sent
to you") until `emailVerified` is true. OTPs expire after 10 minutes
(configurable via app.otp.expiry-minutes). The auth response includes an
`emailVerified` field so the frontend can route unverified users to the
OTP screen.

Email delivery uses the Resend API (app.email.resend-api-key); if no key is
configured the OTP is logged server-side for local development.

Account lockout: after 5 consecutive failed login attempts an account is
locked for 30 minutes (configurable via security.login.*). Locked accounts
return 401 "Account is locked. Please contact an administrator".

Login error feedback (401): the response body includes `remainingAttempts`
(attempts remaining AFTER this failed attempt, e.g. 4 after the first of 5)
on a failed login, and `lockoutSeconds` (countdown until the account unlocks)
when the account is locked.

PATCH /api/auth/users/{userId}/unlock   (admin: unlock an account and reset its failed-login counter)

---

## User

GET /api/users                  (admin: list all users)

GET /api/users/{id}             (admin: get one user)

POST /api/users                 (admin: create user - creates StaffProfile/Client profile by role)

PUT /api/users/{id}             (admin: update user)

DELETE /api/users/{id}          (super admin only: delete user)

PATCH /api/users/{id}/status    (admin: enable/disable - body: { "enabled": true|false })

---

## Staff

GET /api/staff

POST /api/staff

PUT /api/staff/{id}

DELETE /api/staff/{id}

PUT /api/staff/profile          (self-service: complete own profile)

GET /api/staff/profile          (self-service: view own profile)

PATCH /api/staff/{userId}/verification   (admin: approve/reject staff - body: { "verified": true|false })

POST /api/staff/documents       (staff: multipart upload - documentType + file)

GET /api/staff/documents        (staff: list own documents)

GET /api/staff/documents/{id}/download   (staff: download own document)

DELETE /api/staff/documents/{id}        (staff: delete own document)

GET /api/staff/{userId}/documents       (admin: list a staff user's documents)

GET /api/staff/{userId}/documents/{id}/download   (admin: download a staff user's document)

---

## Client

GET /api/clients

POST /api/clients

PUT /api/clients/{id}

PUT /api/clients/profile      (self-service: complete own organization profile)

GET /api/clients/profile      (self-service: view own organization profile)

---

## Assignment

POST /api/assignments

GET /api/assignments

PUT /api/assignments/{id}

---

## Attendance

POST /api/attendance/checkin

POST /api/attendance/checkout

---

## Invoice

GET /api/invoices

POST /api/invoices