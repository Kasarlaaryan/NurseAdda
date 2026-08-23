import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AuthLayout } from '../components/layouts/AuthLayout';
import { MainLayout } from '../components/layouts/MainLayout';
import { ProtectedRoute } from './ProtectedRoute';
import { RoleRoute } from './RoleRoute';
import { StaffVerificationGuard } from './StaffVerificationGuard';

import { LoginPage } from '../pages/LoginPage';
import { RegisterPage } from '../pages/RegisterPage';
import { StaffRegisterPage } from '../pages/StaffRegisterPage';
import { ClientRegisterPage } from '../pages/ClientRegisterPage';
import { CompleteProfilePage } from '../pages/CompleteProfilePage';
import { ForgotPasswordPage } from '../pages/ForgotPasswordPage';
import { ResetPasswordPage } from '../pages/ResetPasswordPage';

import { PublicLayout } from '../components/layouts/PublicLayout';
import { LandingPage } from '../pages/public/LandingPage';
import { AboutPage } from '../pages/public/AboutPage';
import { ServicesPage } from '../pages/public/ServicesPage';
import { ContactPage } from '../pages/public/ContactPage';

import { DashboardPage } from '../pages/DashboardPage';
import { StaffManagementPage } from '../pages/StaffManagementPage';
import { ClientManagementPage } from '../pages/ClientManagementPage';
import { StaffingRequestsPage } from '../pages/StaffingRequestsPage';
import { StaffingRequestCreatePage } from '../pages/StaffingRequestCreatePage';
import { AssignmentsPage } from '../pages/AssignmentsPage';
import { AttendancePage } from '../pages/AttendancePage';
import { InvoicesPage } from '../pages/InvoicesPage';
import { PaymentsPage } from '../pages/PaymentsPage';
import { ReportsPage } from '../pages/ReportsPage';
import { NotificationsPage } from '../pages/NotificationsPage';
import { SettingsPage } from '../pages/SettingsPage';
import { ProfilePage } from '../pages/ProfilePage';
import { UnauthorizedPage } from '../pages/UnauthorizedPage';
import { VerificationManagementPage } from '../pages/VerificationManagementPage';
import { UserManagementPage } from '../pages/UserManagementPage';
import { AssignmentDetailPage } from '../pages/AssignmentDetailPage';
import { StaffProfileDetailPage } from '../pages/StaffProfileDetailPage';
import { NotFoundPage } from '../pages/NotFoundPage';
import { AdminRegisterPage } from '../pages/AdminRegisterPage';

export const AppRoutes: React.FC = () => {
  return (
    <Routes>
      {/* Public Web Pages */}
      <Route element={<PublicLayout />}>
        <Route path="/" element={<LandingPage />} />
        <Route path="/about" element={<AboutPage />} />
        <Route path="/services" element={<ServicesPage />} />
        <Route path="/contact" element={<ContactPage />} />
      </Route>

      {/* Public Auth Routes */}
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/register-staff" element={<StaffRegisterPage />} />
        <Route path="/register-client" element={<ClientRegisterPage />} />
        <Route path="/complete-profile" element={<CompleteProfilePage />} />
        <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        <Route path="/reset-password" element={<ResetPasswordPage />} />
      </Route>

      {/* Protected Enterprise Portal Routes */}
      <Route element={<ProtectedRoute />}>
        <Route element={<MainLayout />}>
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/users" element={<UserManagementPage />} />
          <Route path="/staff" element={<StaffManagementPage />} />
          <Route path="/staff/:id" element={<StaffProfileDetailPage />} />
          <Route path="/clients" element={<ClientManagementPage />} />
          {/* Staff must be verified before accessing work-related pages */}
          <Route element={<StaffVerificationGuard />}>
            <Route path="/requests" element={<StaffingRequestsPage />} />
            <Route path="/requests/create" element={<StaffingRequestCreatePage />} />
            <Route path="/assignments" element={<AssignmentsPage />} />
            <Route path="/assignments/:id" element={<AssignmentDetailPage />} />
            <Route path="/attendance" element={<AttendancePage />} />
          </Route>
          <Route path="/invoices" element={<InvoicesPage />} />
          <Route path="/payments" element={<PaymentsPage />} />
          <Route path="/reports" element={<ReportsPage />} />
          <Route path="/notifications" element={<NotificationsPage />} />
          <Route path="/profile" element={<ProfilePage />} />

          {/* Admin Restricted Verification Route */}
          <Route element={<RoleRoute allowedRoles={['ROLE_SUPER_ADMIN', 'ROLE_ADMIN']} />}>
            <Route path="/verifications" element={<VerificationManagementPage />} />
          </Route>

          {/* Super Admin Restricted Routes */}
          <Route element={<RoleRoute allowedRoles={['ROLE_SUPER_ADMIN']} />}>
            <Route path="/settings" element={<SettingsPage />} />
            <Route path="/users/create-admin" element={<AdminRegisterPage />} />
          </Route>

          <Route path="/unauthorized" element={<UnauthorizedPage />} />
          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Route>
    </Routes>
  );
};
