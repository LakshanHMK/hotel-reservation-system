# LankaStay Hotels & Resorts – Multi-Property Hotel Reservation System

**Module:** SE2030 – Software Engineering
**Focus:** Web-Based Multi-Property Hotel Reservation System
**Hotel Management Owner:** Wickramasinghe M.P.T.H — IT25300115

---

## 🏛️ Architecture Overview

LankaStay is a full-stack hotel reservation and property management web platform built with enterprise security, responsive luxury styling, and robust data persistence:

- **Frontend:** React (Vite), React Router v7, Vanilla CSS luxury design system, Lucide icons.
- **Backend:** Java 17+, Spring Boot 3.4+, Spring Security (HttpOnly Session Cookies, CSRF Protection, BCrypt Hashing, Role-Based Access Control).
- **Database:** MySQL 8+ with Flyway schema migration management (`lankastay_db`).
- **Security:** CSRF cookie token protection, HttpOnly session cookies, role scoping (`MANAGER`, `HOTEL_STAFF`, `CUSTOMER`), audit logging.

---

## 📁 Repository Directory Layout

```
SE Hotel Reservation system/
│
├── backend/                  # Spring Boot Java Backend
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/lankastay/backend/
│   │   │   │   ├── config/       # Spring Security, CORS, Web MVC configuration
│   │   │   │   ├── controller/   # REST Controllers (Auth, Hotels, Destinations, Customer)
│   │   │   │   ├── dto/          # Request & Response Data Transfer Objects
│   │   │   │   ├── entity/       # JPA Entities (Hotel, CustomerUser, StaffUser, etc.)
│   │   │   │   ├── exception/    # Global Exception Handlers
│   │   │   │   ├── mapper/       # Entity DTO Mappers
│   │   │   │   ├── repository/   # Spring Data JPA Repositories
│   │   │   │   ├── security/     # Security Filters & User Details
│   │   │   │   └── service/      # Core Business Services
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── db/migration/  # Flyway SQL Migration Scripts
│   │   └── test/                 # Automated Unit & Integration Test Suite
│   └── pom.xml
│
├── frontend/                 # React + Vite Frontend Application
│   ├── src/
│   │   ├── assets/           # Images, Logos, Icons
│   │   ├── components/       # Reusable UI Components (Navbar, Footer, Cards)
│   │   ├── context/          # State Contexts (CustomerContext, HotelsContext)
│   │   ├── pages/            # Page Views (Home, Hotels, Profile, Login, Register, Management)
│   │   ├── routes/           # AppRoutes & Customer/Staff Route Guards
│   │   ├── services/         # API Service Clients (axios/fetch with credentials)
│   │   ├── App.jsx
│   │   └── main.jsx
│   ├── package.json
│   └── vite.config.js
│
├── docs/                     # Documentation (AUTHENTICATION.md, AUTHORIZATION.md, HELP.md)
├── scripts/                  # Development & QA Scripts (scripts/qa/)
├── uploads/                  # Runtime File Uploads (uploads/destinations/, uploads/hotels/)
├── .env                      # Local Environment Secrets (Git Ignored)
├── .env.example              # Environment Configuration Template
├── .gitignore                # Git Exclusions
├── start-backend.ps1         # PowerShell Backend Launcher
├── start-backend.bat         # Batch Backend Launcher
├── start-frontend.ps1        # PowerShell Frontend Launcher
├── start-frontend.bat        # Batch Frontend Launcher
└── README.md
```

---

## 🚀 Quick Start Guide

### 1. Prerequisites
- **Java JDK:** 17 or higher
- **Node.js:** 18+ & `npm`
- **Database:** MySQL Server running on `localhost:3306` with database `lankastay_db` created.

### 2. Environment Setup
Copy `.env.example` into a local `.env` file at the repository root:
```bash
cp .env.example .env
```
Ensure your local MySQL password matches `DB_PASSWORD` in `.env`.

---

## 🏃 Running the Application

### Launching Backend (Spring Boot - Port 8080)
Using PowerShell:
```powershell
.\start-backend.ps1
```
Or directly via Maven wrapper:
```powershell
cd backend
.\mvnw.cmd spring-boot:run
```
- **Backend API Base:** `http://localhost:8080`

---

### Launching Frontend (React/Vite - Port 5174)
Using PowerShell:
```powershell
.\start-frontend.ps1
```
Or directly from the repository root:
```powershell
npm run dev
```
Or from the frontend directory:
```powershell
cd frontend
npm run dev
```
- **Frontend App URL:** `http://localhost:5174`

---

## 🧪 Testing & Verification

### Backend Tests (JUnit & Spring Boot Integration Tests)
```powershell
cd backend
.\mvnw.cmd test
```

### Frontend Code Quality & Build Verification
```powershell
cd frontend
npm run lint
npm run build
```

---

## 🔐 Security & Access Summary

- **Session Security:** Server-side sessions with `LANKASTAY_SESSION` HttpOnly cookies (`SameSite=Lax`).
- **CSRF Protection:** Double-Submit Cookie CSRF protection active on non-GET endpoints.
- **Password Security:** BCrypt password hashing (strength 12).
- **Staff Access Portal:** `/staff-login` for Manager & Hotel Staff operations.
- **Customer Access Portal:** `/login` & `/register` for guest bookings & profile management.
