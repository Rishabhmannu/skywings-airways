# SkyWings Airways

A full-stack airline ticket booking system built with **Java 21, Spring Boot 3.3, and React 18**. Features real-time flight data from Google Flights, simulated payments with dual-channel OTP verification (SMS + Email), and PDF e-ticket generation with QR codes.

> Originally built as a 3rd semester OOP coursework project (Java Swing), now completely rewritten into a production-grade full-stack application.

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

## Tech Stack

| Layer | Technologies |
|-------|-------------|
| **Backend** | Java 21, Spring Boot 3.3, Spring Security (JWT), Spring Data JPA, PostgreSQL, Redis |
| **Frontend** | React 18, Vite, Tailwind CSS, Axios, React Router |
| **Integrations** | SerpAPI (Google Flights), Twilio (SMS OTP), SendGrid (Email OTP), OpenPDF + ZXing (PDF + QR) |
| **DevOps** | Docker Compose, Shell scripts for start/shutdown |
| **Testing** | JUnit 5, Mockito, MockMvc, Integration tests (55 tests) |

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

## Quick Start

### Prerequisites

- Java 21 (Eclipse Temurin)
- Maven 3.9+
- Node.js 20+
- Docker Desktop

### 1. Clone & configure

```bash
git clone https://github.com/<your-username>/skywings-airways.git
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
| SendGrid | 100 emails/day | [sendgrid.com](https://sendgrid.com) — Use Single Sender Verification |
| Twilio | ~1,900 SMS with trial credit | [twilio.com](https://twilio.com) — Add Verified Caller IDs for trial |
| SerpAPI | 100 searches/month | [serpapi.com](https://serpapi.com) — Google Flights engine |

All services work without API keys configured — the app falls back gracefully (email OTP logs to console, flights served from database, SMS skipped).

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
├── IMPLEMENTATION_PLAN.md          Detailed architecture & design document
└── README.md
```

## Running Tests

```bash
cd backend
mvn test
```

55 tests covering: BookingService, PricingService (with fare discounts), AuthService, PaymentService, OtpService, LuhnValidator, AuthController, FlightController, and a full booking flow integration test.

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
   │         │ OpenPDF     │──→ Branded PDF e-tickets
   │         │ ZXing       │──→ QR codes (scannable booking data)
   │         │ Redis       │──→ OTP storage + flight cache
   │         └─────────────┘
```

## License

Portfolio project by **Rishabh Kumar**. Built as a complete evolution of a 3rd semester OOP coursework project at IIIT Allahabad.
