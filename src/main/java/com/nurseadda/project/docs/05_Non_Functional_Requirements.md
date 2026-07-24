# Non-Functional Requirements

## Purpose

This document defines the quality attributes and operational requirements of the NurseAdda application.

---

## Performance

- API response time should be less than 2 seconds under normal load.
- The system should support at least 500 concurrent users.
- Database queries should be optimized for fast search.

---

## Security

- JWT-based authentication.
- Passwords encrypted using BCrypt.
- Role-Based Access Control (RBAC).
- HTTPS communication.
- Input validation on all APIs.
- Protection against SQL Injection and XSS attacks.

---

## Scalability

- Modular architecture.
- RESTful APIs.
- Stateless authentication.
- Horizontal scalability supported.

---

## Availability

- Target uptime: 99.9%.
- Automated backups.
- Error logging and monitoring.

---

## Reliability

- Proper exception handling.
- Transaction management.
- Data consistency.

---

## Maintainability

- Layered architecture.
- SOLID principles.
- Clean coding standards.
- Proper documentation.

---

## Compatibility

Backend:
- Java 21
- Spring Boot 3.x

Frontend:
- React
- TypeScript

Database:
- MySQL 8+

Browsers:
- Chrome
- Firefox
- Edge