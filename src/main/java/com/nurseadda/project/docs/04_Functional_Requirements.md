# Functional Requirements

## Introduction

This document defines the functional requirements of the **NurseAdda – Enterprise Healthcare Workforce Management System**. These requirements describe the features and functionalities the system must provide to support healthcare staffing operations efficiently.


# 1. Authentication Module

The Authentication Module ensures secure access to the application using role-based authentication and authorization.

## Functional Requirements

### User Registration

- Allow healthcare professionals, administrators, and clients to register.
- Validate email address and mobile number uniqueness.
- Encrypt passwords before storing them.
- Assign the appropriate user role during registration.

### Login

- Authenticate users using email and password.
- Generate JWT access token after successful authentication.
- Redirect users to their respective dashboards based on assigned roles.

### Logout

- Allow authenticated users to securely log out.
- Invalidate user session or JWT token on logout.

### Forgot Password

- Allow users to request a password reset.
- Send password reset instructions through email.
- Verify reset token before allowing password change.

### Change Password

- Allow authenticated users to update their password.
- Validate current password before updating.
- Encrypt the new password before storing it.

### JWT Authentication

- Secure all protected REST APIs using JWT.
- Validate JWT token for every authenticated request.
- Restrict unauthorized access to protected resources.


# 2. User Management Module

The User Management Module manages user profiles and role-based access.

## Functional Requirements

### Profile Creation

- Create user profile after successful registration.
- Store personal and contact information.

### Profile Update

- Allow users to update their profile information.
- Maintain profile history if required.

### Role Management

- Assign user roles.
- Update user roles by authorized administrators.
- Restrict system functionality based on assigned roles.


# 3. Staff Management Module

The Staff Management Module manages healthcare professionals and their credentials.

## Functional Requirements

### Personal Details

- Store personal information.
- Contact details.
- Address.
- Emergency contact.

### Skills

- Maintain healthcare skills and specialization.
- Allow multiple skills per healthcare professional.

### Experience

- Store years of experience.
- Previous employment details.
- Areas of expertise.

### Certifications

- Store licenses and certifications.
- Maintain expiry dates.
- Track document renewal requirements.

### Availability

- Update current availability status.
- Manage work shifts and schedules.
- Prevent assignment conflicts.

### Document Upload

- Upload qualification certificates.
- Upload government identification.
- Upload professional licenses.

### Verification

- Administrator verifies uploaded documents.
- Approve or reject submitted documents.
- Mark healthcare professional as verified.



# 4. Client Management Module

The Client Management Module manages hospitals, clinics, and healthcare organizations.

## Functional Requirements

### Organization Profile

- Create client organization.
- Store organization information.
- Maintain organization profile.

### Branch Management

- Add multiple branches.
- Manage branch information.
- Associate staffing requests with branches.

### Contact Management

- Maintain organization contact persons.
- Store contact details.
- Update communication information.


# 5. Staffing Request Module

The Staffing Request Module manages healthcare staffing requests from client organizations.

## Functional Requirements

### Create Staffing Request

- Create new staffing request.
- Specify required designation.
- Define location, shift, duration, and required skills.

### Search Professionals

- Search professionals using:
    - Skills
    - Experience
    - Location
    - Availability
    - Certification
    - Specialization

### Assign Staff

- Assign verified professionals.
- Notify assigned healthcare professionals.
- Track assignment acceptance.

### Track Assignment

- Monitor assignment status.
- View assignment history.
- Update assignment progress.


# 6. Attendance Management Module

The Attendance Module records attendance for assigned healthcare professionals.

## Functional Requirements

### Check-In

- Record daily check-in time.
- Validate active assignment before check-in.

### Check-Out

- Record daily check-out time.
- Calculate working hours.

### Timesheet Management

- Generate daily timesheets.
- Maintain attendance history.
- Generate attendance reports.


# 7. Invoice Management Module

The Invoice Module manages billing for completed staffing assignments.

## Functional Requirements

### Invoice Generation

- Automatically generate invoices after assignment completion.
- Calculate billing based on assignment details.
- Maintain invoice history.

### Payment Tracking

- Track payment status.
- Record payment details.
- Maintain payment history.


# 8. Reports & Dashboard Module

The Reporting Module provides operational and business insights.

## Functional Requirements

### Dashboard

Display:

- Total Healthcare Professionals
- Verified Professionals
- Active Assignments
- Pending Requests
- Attendance Summary
- Invoice Summary
- Payment Summary

### Staff Reports

Generate reports based on:

- Skills
- Experience
- Verification Status
- Availability

### Attendance Reports

Generate:

- Daily Attendance
- Monthly Attendance
- Timesheets
- Working Hours

### Revenue Reports

Generate:

- Invoice Summary
- Payment Summary
- Revenue Analytics
- Outstanding Payments


# Functional Requirement Summary

| Module | Key Functionalities |
|----------|---------------------|
| Authentication | Registration, Login, JWT, Password Management |
| User Management | Profile Management, Role Management |
| Staff Management | Staff Profile, Skills, Experience, Certifications, Verification |
| Client Management | Organizations, Branches, Contacts |
| Staffing Request | Create Requests, Search Staff, Assign Professionals |
| Attendance | Check-In, Check-Out, Timesheets |
| Invoice | Invoice Generation, Billing |
| Payment | Payment Tracking, Payment History |
| Reports | Dashboard, Staff Reports, Attendance Reports, Revenue Reports |