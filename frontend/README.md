# NurseAdda Workforce Management — Frontend

A modern React-based healthcare workforce management platform for hospitals, clinics, and healthcare professionals across India.

## Tech Stack

| Category | Technology |
|---|---|
| Framework | React 19 + TypeScript |
| Build Tool | Vite 6 |
| Styling | Tailwind CSS 4 |
| State Management | Zustand |
| Server State | TanStack React Query |
| Routing | React Router DOM 7 |
| HTTP Client | Axios |
| Animations | Motion (Framer Motion) |
| Icons | Lucide React |
| PDF Generation | html2pdf.js |

## Project Structure

```
frontend/src/
├── components/
│   ├── common/          # Reusable UI primitives
│   │   ├── Avatar.tsx
│   │   ├── Badge.tsx
│   │   ├── Button.tsx
│   │   ├── Card.tsx
│   │   ├── ConfirmationDialog.tsx
│   │   ├── Drawer.tsx
│   │   ├── EmptyState.tsx
│   │   ├── ErrorBoundary.tsx
│   │   ├── Input.tsx
│   │   ├── Loader.tsx
│   │   ├── Modal.tsx
│   │   ├── Pagination.tsx
│   │   ├── Table.tsx
│   │   ├── Tabs.tsx
│   │   └── Toast.tsx
│   └── layouts/         # Page layout wrappers
│       ├── AuthLayout.tsx
│       ├── Footer.tsx
│       ├── LeftSidebar.tsx
│       ├── MainLayout.tsx
│       ├── PageHeader.tsx
│       ├── PublicLayout.tsx
│       ├── SessionTimeoutModal.tsx
│       └── TopNavbar.tsx
├── constants/           # App-wide constants & role configs
├── pages/               # Route-level page components
│   ├── public/          # Public marketing pages
│   │   ├── LandingPage.tsx
│   │   ├── AboutPage.tsx
│   │   ├── ServicesPage.tsx
│   │   └── ContactPage.tsx
│   ├── LoginPage.tsx
│   ├── RegisterPage.tsx         # Role selector
│   ├── StaffRegisterPage.tsx    # Staff registration
│   ├── ClientRegisterPage.tsx   # Facility registration
│   ├── DashboardPage.tsx
│   ├── StaffManagementPage.tsx
│   ├── StaffProfileDetailPage.tsx
│   ├── ClientManagementPage.tsx
│   ├── UserManagementPage.tsx
│   ├── VerificationManagementPage.tsx
│   ├── StaffingRequestsPage.tsx
│   ├── AssignmentsPage.tsx
│   ├── AssignmentDetailPage.tsx
│   ├── AttendancePage.tsx
│   ├── InvoicesPage.tsx
│   ├── PaymentsPage.tsx
│   ├── ReportsPage.tsx
│   ├── NotificationsPage.tsx
│   ├── ProfilePage.tsx
│   ├── SettingsPage.tsx
│   └── ...
├── routes/              # Route definitions & guards
│   ├── AppRoutes.tsx
│   ├── ProtectedRoute.tsx
│   └── RoleRoute.tsx
├── services/            # API client & service layers
│   ├── apiClient.ts           # Axios instance with auth interceptors
│   ├── authService.ts         # Auth, registration, profile endpoints
│   ├── assignmentService.ts   # Staffing requests & assignments
│   ├── attendanceService.ts   # Check-in / check-out
│   ├── invoiceService.ts      # Invoices, payments, rates, billing
│   ├── staffService.ts        # Staff management & verification
│   ├── clientService.ts       # Client profile & user management
│   └── mockData.ts            # Static mock data (legacy)
├── store/               # Zustand state stores
│   ├── useAuthStore.ts        # Auth state, login, register, logout
│   ├── useAppStore.ts         # UI state (sidebar, search)
│   ├── useThemeStore.ts       # Dark/light theme toggle
│   └── useNotificationStore.ts
├── types/               # TypeScript type definitions
│   └── index.ts
├── App.tsx
├── main.tsx
└── index.css
```

## Getting Started

### Prerequisites
 r
- Node.js 18+
- Backend running at `http://localhost:8080` (for API proxy)

### Installation

```bash
cd frontend
npm install
```

### Environment Setup

The Vite dev server proxies `/api` requests to the Spring Boot backend. No `.env` file is required for local development — the proxy is configured in `vite.config.ts`:

```ts
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  },
}
```

### Run Development Server

```bash
npm run dev
```

The app starts at `http://localhost:3000`.

### Build for Production

```bash
npm run build
```

Output goes to `dist/`.

### Type Check

```bash
npm run lint
```

Runs `tsc --noEmit` to check for TypeScript errors without emitting files.

## Key Features

### Authentication & Authorization

- **Registration** — Separate flows for healthcare professionals (`/register-staff`) and facility partners (`/register-client`) with OTP email verification
- **Login** — Shared login page for all roles
- **JWT** — Access + refresh token flow with automatic token refresh via Axios interceptors
- **Role-Based Access Control** — Four roles: `ROLE_SUPER_ADMIN`, `ROLE_ADMIN`, `ROLE_STAFF`, `ROLE_USER`
- **Route Guards** — `ProtectedRoute` (requires auth) and `RoleRoute` (restricts by role)

### Role-Based Pages

| Role | Accessible Pages |
|---|---|
| **Super Admin** | All pages including Settings |
| **Admin** | Dashboard, Staff, Clients, Users, Verifications, Requests, Assignments, Attendance, Invoices, Payments |
| **Staff (Nurse)** | Dashboard, My Assignments, My Attendance, Profile |
| **Client (Facility)** | Dashboard, My Staff Assignments, My Invoices, Profile |

### API Integration

All data pages use **TanStack React Query** for:
- Automatic caching & background refetching
- Loading & error states
- Mutation handling with cache invalidation

Backend endpoints consumed:

| Service | Endpoints |
|---|---|
| `authService` | `/api/auth/login`, `/register-*`, `/send-otp`, `/verify-otp`, `/refresh`, `/me`, `/staff-profile`, `/client-profile`, `/admin/users`, etc. |
| `assignmentService` | `/api/staffing-requests`, `/api/assignments` |
| `attendanceService` | `/api/attendance/checkin`, `/checkout`, `/today`, `/my` |
| `invoiceService` | `/api/invoices`, `/api/payments`, `/api/rates`, `/api/billing/summary` |
| `staffService` | `/api/auth/staff`, `/api/auth/staff/{id}`, `/api/auth/staff/{id}/verification` |

### UI Components

All UI components are in `src/components/common/` and follow a consistent design system:

- **Design** — Dark-first with slate/sky color palette, rounded-2xl corners, bold typography
- **Theming** — Dark/light mode toggle via `useThemeStore` + Tailwind `dark:` classes
- **Responsive** — Mobile-first with responsive grid layouts
- **Accessibility** — Semantic HTML, keyboard navigation, focus states

## Architecture Notes

- **apiClient.ts** — Central Axios instance with request interceptor (adds Bearer token) and response interceptor (auto-refreshes expired tokens)
- **Zustand stores** — Lightweight state management with `persist` middleware for auth state (survives page refresh)
- **Service layer** — Each domain has its own service file that wraps `apiClient` calls with typed request/response interfaces
- **No global CSS framework config** — Tailwind v4 uses the Vite plugin (`@tailwindcss/vite`) with CSS-first configuration
