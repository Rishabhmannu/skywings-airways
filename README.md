# SkyWings Airways

A full-stack airline ticket booking system built with Spring Boot and React. Features real-time flight data from Google Flights, simulated payments with dual-channel OTP verification (SMS + Email), and PDF e-ticket generation with QR codes.

## Tech Stack

**Backend:** Java 21, Spring Boot 3.3, Spring Security (JWT), Spring Data JPA, PostgreSQL, Redis, Thymeleaf

**Frontend:** React 18, Vite, Tailwind CSS, Axios, React Router

**Integrations:** SerpAPI (Google Flights), Twilio (SMS OTP), SendGrid (Email OTP), OpenPDF + ZXing (PDF tickets with QR codes)

## Features

- **Real-time flight search** — Live data from Google Flights via SerpAPI (7,913+ airports with autocomplete)
- **User authentication** — JWT-based auth with email verification OTP on signup
- **Multi-step booking** — Class selection, passenger details (gender, DOB, meal, accessibility), fare discounts
- **Simulated payment** — Luhn card validation + dual-channel OTP (SMS via Twilio + Email via SendGrid)
- **E-tickets** — Branded PDF with QR code (scannable with booking details), emailed on confirmation
- **Special fares** — Student (10%), Armed Forces (15%), Senior Citizen (12%), Medical (10%)
- **Round trip search** — One-way and round-trip flight search support
- **Admin dashboard** — Flight management, booking overview, user management, revenue stats
- **Booking management** — View history, cancel with 25% penalty, download tickets

## Quick Start

### Prerequisites

- Java 21 (Eclipse Temurin)
- Maven 3.9+
- Node.js 20+
- Docker Desktop

### 1. Clone & configure

```bash
git clone <repo-url>
cd java-oops-project
cp .env.example .env
# Edit .env with your API keys (SendGrid, Twilio, SerpAPI)
```

### 2. Start everything

```bash
./start.sh
```

This starts PostgreSQL, Redis (Docker), the Spring Boot backend, and the React frontend. It runs health checks on all services.

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

## API Keys Setup

| Service | Free Tier | Get Keys |
|---------|-----------|----------|
| SendGrid | 100 emails/day | [sendgrid.com](https://sendgrid.com) — Single Sender Verification |
| Twilio | ~1,900 SMS with trial credit | [twilio.com](https://twilio.com) — Verify caller IDs for trial |
| SerpAPI | 100 searches/month | [serpapi.com](https://serpapi.com) |

## Project Structure

```
├── backend/                    Spring Boot application
│   ├── src/main/java/com/skywings/
│   │   ├── config/             Security, Redis, OpenAPI, JWT filter
│   │   ├── controller/         REST API endpoints
│   │   ├── dto/                Request/Response DTOs
│   │   ├── entity/             JPA entities + enums
│   │   ├── exception/          Custom exceptions + global handler
│   │   ├── repository/         Spring Data JPA repositories
│   │   ├── service/            Business logic
│   │   └── util/               Luhn validator, transaction ID generator
│   └── src/test/               55 tests (unit + controller + integration)
├── frontend/                   React SPA
│   └── src/
│       ├── components/         Reusable UI components
│       ├── context/            Auth context provider
│       ├── pages/              All page components
│       └── api/                Axios instance with JWT interceptor
├── docker-compose.yml          PostgreSQL + Redis
├── start.sh                    Start all services
├── shutdown.sh                 Stop all services
└── IMPLEMENTATION_PLAN.md      Detailed architecture & design doc
```

## Running Tests

```bash
cd backend
mvn test
```

55 tests: unit (BookingService, PricingService, AuthService, PaymentService, OtpService, LuhnValidator), controller (AuthController, FlightController), and integration (full booking flow).

## Architecture

```
React SPA ──→ Spring Boot REST API ──→ PostgreSQL
   │                │                       │
   │          JWT Auth Filter          JPA/Hibernate
   │                │
   │         ┌──────┴──────┐
   │         │   Services  │
   │         ├─────────────┤
   │         │ SerpAPI     │──→ Google Flights (real-time)
   │         │ Twilio      │──→ SMS OTP
   │         │ SendGrid    │──→ Email OTP + confirmations
   │         │ OpenPDF     │──→ PDF e-tickets
   │         │ ZXing       │──→ QR codes
   │         │ Redis       │──→ OTP storage + flight cache
   │         └─────────────┘
```

## License

Portfolio project by Rishabh Kumar. Built as an evolution of a 3rd semester OOP coursework project.
