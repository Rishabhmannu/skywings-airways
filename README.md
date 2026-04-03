# ✈ SkyWings Airways

<div align="center">

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-3-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker&logoColor=white)

**A full-stack airline ticket booking system with real-time Google Flights data, simulated payments with dual-channel OTP, and PDF e-ticket generation.**

[Live App](https://skywings-airways.vercel.app) &bull; [API Docs (Swagger)](http://150.136.157.197:8080/swagger-ui.html) &bull; [Report Bug](https://github.com/Rishabhmannu/skywings-airways/issues)

</div>

> Originally built as a 3rd semester OOP coursework project (Java Swing), now completely rewritten into a production-grade full-stack application.

---

## Screenshots

<table>
  <tr>
    <td><img src="screenshots/landing-page.png" alt="Landing Page" width="100%"/><br/><em>Landing Page — Flight Search with Autocomplete</em></td>
    <td><img src="screenshots/flight-listings.png" alt="Flight Listings" width="100%"/><br/><em>Real-time Flight Results from Google Flights</em></td>
  </tr>
  <tr>
    <td><img src="screenshots/bookings.png" alt="Bookings" width="100%"/><br/><em>Booking Management — View, Cancel, Download</em></td>
    <td><img src="screenshots/ticket.png" alt="E-Ticket" width="100%"/><br/><em>Branded PDF E-Ticket with QR Code</em></td>
  </tr>
</table>

---

## Tech Stack

### Backend
![Java](https://img.shields.io/badge/Java_21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.3-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-59666C?style=flat-square&logo=hibernate&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat-square&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=flat-square&logo=redis&logoColor=white)

### Frontend
![React](https://img.shields.io/badge/React_18-61DAFB?style=flat-square&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=flat-square&logo=vite&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-06B6D4?style=flat-square&logo=tailwindcss&logoColor=white)
![React Router](https://img.shields.io/badge/React_Router-CA4245?style=flat-square&logo=reactrouter&logoColor=white)
![Axios](https://img.shields.io/badge/Axios-5A29E4?style=flat-square&logo=axios&logoColor=white)

### Integrations
![Google](https://img.shields.io/badge/Google_Flights_(SerpAPI)-4285F4?style=flat-square&logo=google&logoColor=white)
![Twilio](https://img.shields.io/badge/Twilio_SMS-F22F46?style=flat-square&logo=twilio&logoColor=white)
![SendGrid](https://img.shields.io/badge/SendGrid_Email-51A9E3?style=flat-square&logo=twilio&logoColor=white)
![OpenPDF](https://img.shields.io/badge/OpenPDF-FF6600?style=flat-square&logo=adobeacrobatreader&logoColor=white)
![ZXing](https://img.shields.io/badge/ZXing_QR-000000?style=flat-square&logo=qrcode&logoColor=white)

### Deployment
![Vercel](https://img.shields.io/badge/Vercel-000000?style=flat-square&logo=vercel&logoColor=white)
![Oracle Cloud](https://img.shields.io/badge/Oracle_Cloud-F80000?style=flat-square&logo=oracle&logoColor=white)
![Neon](https://img.shields.io/badge/Neon_PostgreSQL-00E599?style=flat-square&logo=postgresql&logoColor=black)

### Testing
![JUnit5](https://img.shields.io/badge/JUnit_5-25A162?style=flat-square&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-78A641?style=flat-square)
![55 Tests](https://img.shields.io/badge/55_Tests_Passing-brightgreen?style=flat-square)

---

## Deployment

The application is deployed across multiple cloud services — all on **free tiers ($0/month)**:

```
                    ┌──────────────────────────────┐
                    │     skywings-airways.         │
    Users ────────▶ │       vercel.app              │ (React SPA)
                    │     Vercel (CDN)              │
                    └──────────┬───────────────────┘
                               │ /api/* proxy
                               ▼
                    ┌──────────────────────────────┐
                    │  Oracle Cloud VM             │
                    │  150.136.157.197:8080         │ (Spring Boot)
                    │  Always Free ARM/AMD          │
                    │  Ubuntu 22.04 + Java 21       │
                    └──────┬──────────┬────────────┘
                           │          │
                ┌──────────▼──┐  ┌────▼──────────┐
                │ Neon        │  │ SendGrid      │
                │ PostgreSQL  │  │ Email OTP     │
                │ (Free tier) │  │ (100/day)     │
                └─────────────┘  └───────────────┘
                                 ┌───────────────┐
                                 │ Twilio        │
                                 │ SMS OTP       │
                                 │ (Trial)       │
                                 └───────────────┘
                                 ┌───────────────┐
                                 │ SerpAPI       │
                                 │ Google Flights│
                                 │ (100/month)   │
                                 └───────────────┘
```

| Service | Provider | Tier | Purpose |
|---------|----------|------|---------|
| **Frontend** | ![Vercel](https://img.shields.io/badge/Vercel-000?style=flat-square&logo=vercel&logoColor=white) | Free | React SPA hosting, global CDN, API proxy |
| **Backend** | ![Oracle](https://img.shields.io/badge/Oracle_Cloud-F80000?style=flat-square&logo=oracle&logoColor=white) | Always Free | Spring Boot on Ubuntu VM, 24/7 uptime |
| **Database** | ![Neon](https://img.shields.io/badge/Neon-00E599?style=flat-square&logo=postgresql&logoColor=black) | Free (0.5 GB) | PostgreSQL 17, serverless, auto-suspend |
| **Email** | ![SendGrid](https://img.shields.io/badge/SendGrid-51A9E3?style=flat-square&logo=twilio&logoColor=white) | Free (100/day) | OTP emails, booking confirmations |
| **SMS** | ![Twilio](https://img.shields.io/badge/Twilio-F22F46?style=flat-square&logo=twilio&logoColor=white) | Trial credit | SMS OTP for payment verification |
| **Flights** | ![SerpAPI](https://img.shields.io/badge/SerpAPI-4285F4?style=flat-square&logo=google&logoColor=white) | Free (100/mo) | Real-time Google Flights data |

---

## Features

- **Real-time flight search** — Live data from Google Flights via SerpAPI with 7,913+ airport autocomplete
- **Visual seat map** — Interactive seat picker with 2-3-2 cabin layout
- **User authentication** — JWT auth with email verification OTP on signup
- **Multi-step booking** — Class selection, passenger details (gender, DOB, meal preference, accessibility needs), fare discounts
- **Simulated payment** — Luhn card validation + dual-channel OTP (SMS via Twilio + Email via SendGrid). No real money charged.
- **E-tickets** — Branded PDF with QR code (scannable booking details), emailed automatically on confirmation
- **Special fares** — Student (10% off), Armed Forces (15%), Senior Citizen (12%), Doctors & Nurses (10%)
- **Round trip support** — One-way and round-trip flight search
- **Admin dashboard** — Flight CRUD, booking management with passenger details, user role management, revenue stats
- **Booking management** — View history, cancel with 25% penalty, download tickets
- **Input validation** — Server-side validation on all endpoints, XSS sanitization, password strength enforcement
- **Error handling** — Global exception handler, structured API error responses, 404 page

---

## Quick Start (Local Development)

### Prerequisites

- Java 21 (Eclipse Temurin)
- Maven 3.9+
- Node.js 20+
- Docker Desktop

### 1. Clone & configure

```bash
git clone https://github.com/Rishabhmannu/skywings-airways.git
cd skywings-airways
cp .env.example .env
# Edit .env with your API keys (SendGrid, Twilio, SerpAPI)
```

### 2. Start everything

```bash
./start.sh
```

This starts PostgreSQL + Redis (Docker), the Spring Boot backend, and the React frontend. Runs health checks on all 5 services.

### 3. Open the app

| Service | URL |
|---------|-----|
| App | http://localhost:5173 |
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |

### 4. Shut down

```bash
./shutdown.sh
```

---

## Test Accounts

| Role | Email | Password |
|------|-------|----------|
| Admin | admin@skywings.com | Admin@123 |
| Passenger | Sign up from the UI | — |

## Test Card Numbers

| Card | Number | Luhn Valid |
|------|--------|-----------|
| Visa (test) | 4532 0151 1283 0366 | Yes |
| Visa (common) | 4111 1111 1111 1111 | Yes |
| Invalid | 1234 5678 9012 3456 | No |

Use any future expiry (e.g., 12/28) and any 3-digit CVV.

---

## API Keys Setup

| Service | Free Tier | Get Keys |
|---------|-----------|----------|
| ![SendGrid](https://img.shields.io/badge/SendGrid-51A9E3?style=flat-square&logo=twilio&logoColor=white) | 100 emails/day | [sendgrid.com](https://sendgrid.com) — Use Single Sender Verification |
| ![Twilio](https://img.shields.io/badge/Twilio-F22F46?style=flat-square&logo=twilio&logoColor=white) | ~1,900 SMS with trial credit | [twilio.com](https://twilio.com) — Add Verified Caller IDs for trial |
| ![SerpAPI](https://img.shields.io/badge/SerpAPI-4285F4?style=flat-square&logo=google&logoColor=white) | 100 searches/month | [serpapi.com](https://serpapi.com) — Google Flights engine |

All services work without API keys configured — the app falls back gracefully (email OTP logs to console, flights served from database, SMS skipped).

---

## Project Structure

```
├── backend/                        Spring Boot application
│   ├── src/main/java/com/skywings/
│   │   ├── config/                 Security, Redis, OpenAPI, JWT filter, data seeder
│   │   ├── controller/             REST API endpoints (7 controllers)
│   │   ├── dto/                    Request/Response DTOs (18 classes)
│   │   ├── entity/                 JPA entities + enums (6 entities, 6 enums)
│   │   ├── exception/              Custom exceptions + global handler
│   │   ├── repository/             Spring Data JPA repositories
│   │   ├── service/                Business logic (12 services)
│   │   └── util/                   Luhn validator, input sanitizer, transaction ID generator
│   └── src/test/                   55 tests (unit + controller + integration)
├── frontend/                       React SPA
│   └── src/
│       ├── components/             Reusable UI (seat map, airport search, flight cards)
│       ├── context/                Auth context provider
│       ├── pages/                  16 page components (public + passenger + admin)
│       └── api/                    Axios instance with JWT interceptor
├── screenshots/                    App screenshots
├── docker-compose.yml              PostgreSQL + Redis containers
├── start.sh                        Start all services with health checks
├── shutdown.sh                     Graceful shutdown of all services
├── .env.example                    Environment variable template
└── README.md
```

---

## Running Tests

```bash
cd backend
mvn test
```

55 tests covering: BookingService, PricingService (with fare discounts), AuthService, PaymentService, OtpService, LuhnValidator, AuthController, FlightController, and a full booking flow integration test.

---

## Architecture

```
React SPA ──→ Spring Boot REST API ──→ PostgreSQL (Neon)
   │                │
   │          JWT Auth Filter
   │                │
   │         ┌──────┴──────┐
   │         │   Services  │
   │         ├─────────────┤
   │         │ SerpAPI     │──→ Google Flights (real-time)
   │         │ Twilio      │──→ SMS OTP
   │         │ SendGrid    │──→ Email OTP + confirmations
   │         │ OpenPDF     │──→ Branded PDF e-tickets
   │         │ ZXing       │──→ QR codes (scannable booking data)
   │         │ OtpStore    │──→ In-memory OTP storage (Redis in dev)
   │         └─────────────┘
```

---

## License

Portfolio project by **Rishabh Kumar** ([@Rishabhmannu](https://github.com/Rishabhmannu)).
Built as a complete evolution of a 3rd semester OOP coursework project at **IIIT Allahabad**.

<div align="center">

Built with Claude Code

![Built with Claude](https://img.shields.io/badge/Built_with-Claude_Code-blueviolet?style=flat-square)

</div>
