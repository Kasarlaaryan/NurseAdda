# Super Admin Management API

All endpoints under `/api/admin/**` are accessible **only** to users with the
`ROLE_SUPER_ADMIN` authority (see `SecurityConfig`).

Base path: `/api/admin`

## Users

| Method | Path                      | Description                                   |
|--------|---------------------------|-----------------------------------------------|
| GET    | `/api/admin/users`        | List all users (paginated, optional role filter) |
| GET    | `/api/admin/users/{id}`   | Get a single user by id                        |
| POST   | `/api/admin/users`        | Create a user / staff / admin account directly |
| PUT    | `/api/admin/users/{id}`   | Update a user (name, email, phone, password, role) |
| DELETE | `/api/admin/users/{id}`   | Permanently delete a user and related records |
| PATCH  | `/api/admin/users/{id}/status` | Enable / disable (reject) a user account  |

### GET `/api/admin/users`

Query params:

- `page` (default `0`)
- `size` (default `10`, max `100`)
- `role` (optional, e.g. `ROLE_STAFF`, `ROLE_USER`, `ROLE_ADMIN`)

Response: Spring `Page<UserResponseDto>`.

### POST `/api/admin/users`

```json
{
  "firstName": "Rohan",
  "lastName": "Mehta",
  "email": "rohan@nurseadda.com",
  "phone": "9876543210",
  "password": "secret123",
  "role": "ROLE_STAFF",
  "staffCategory": "ICU Nurse"
}
```

Notes:

- The account is created instantly with the supplied password (no OTP flow).
- `role` can be `ROLE_STAFF`, `ROLE_USER` or `ROLE_ADMIN`. Creating a
  `ROLE_SUPER_ADMIN` is not allowed.
- `staffCategory` is required when `role = ROLE_STAFF`.
- A `Client` record is created for `ROLE_USER`; a `StaffProfile` for
  `ROLE_STAFF`; no profile for `ROLE_ADMIN`.

### PUT `/api/admin/users/{id}`

All fields optional (partial update). Supports changing the role; changing a
user to/from staff automatically creates or removes the `StaffProfile` and its
documents. Guards:

- Cannot change your own role.
- Cannot modify another `SUPER_ADMIN` account.
- Cannot assign the `SUPER_ADMIN` role.

### PATCH `/api/admin/users/{id}/status` (reject user)

```json
{ "enabled": false }
```

Disabling an account (soft reject) prevents login but keeps the data, and can
be reversed with `{ "enabled": true }`. Guards: cannot disable your own account
or another `SUPER_ADMIN`.

## Staff

| Method | Path                            | Description                              |
|--------|---------------------------------|------------------------------------------|
| GET    | `/api/admin/staff`              | List all staff profiles (paginated)      |
| GET    | `/api/admin/staff/{userId}`     | Get staff profile by user id             |
| PUT    | `/api/admin/staff/{userId}`     | Update staff profile details             |
| PATCH  | `/api/admin/staff/{userId}/verification` | Verify or reject the whole profile |

## Staff documents

| Method | Path                                                    | Description                        |
|--------|---------------------------------------------------------|------------------------------------|
| GET    | `/api/admin/staff/{userId}/documents`                   | List all documents of a staff user |
| PATCH  | `/api/admin/staff/{userId}/documents/{documentId}/verification` | Verify or reject one document  |

### Document verification behaviour

- Each uploaded document carries its own `verified` flag.
- When a document is verified and **all** documents of the profile are now
  verified, the whole staff profile is automatically marked verified and the
  "profile verified" email is sent.
- When a document is rejected, the profile is immediately un-verified and the
  "profile rejected" email is sent.
- Verifying a document belonging to another profile returns `404`.

## Error handling

- `400` – validation failures, illegal admin actions (self-delete, guarding
  SUPER_ADMIN accounts, missing staff category, etc.), invalid query params.
- `404` – user / staff profile / document not found.
- `409` – duplicate email on create/update.
