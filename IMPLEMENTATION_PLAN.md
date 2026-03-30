# SkyWings Airways - Complete Rewrite Implementation Plan

> **Single Source of Truth** for the airline ticket booking system rebuild.
> Original project: 3rd semester OOP coursework (Java Swing, in-memory storage).
> Target: Production-grade Spring Boot application deployable to the cloud.

---

## Table of Contents

1. [Tech Stack & Versions](#1-tech-stack--versions)
2. [Project Structure](#2-project-structure)
3. [Database Schema](#3-database-schema)
4. [Entity Design & Relationships](#4-entity-design--relationships)
5. [API Design — All Endpoints](#5-api-design--all-endpoints)
6. [Authentication & Authorization](#6-authentication--authorization)
7. [Core Business Logic](#7-core-business-logic)
8. [Real-Time Flight Data — Amadeus API Integration](#8-real-time-flight-data--amadeus-api-integration)
9. [Payment Simulation System](#9-payment-simulation-system)
10. [OTP Verification — Email + SMS (Dual Channel)](#10-otp-verification--email--sms-dual-channel)
11. [E-Ticket & Boarding Pass Generation](#11-e-ticket--boarding-pass-generation)
12. [Email Notification System](#12-email-notification-system)
13. [Frontend (React SPA)](#13-frontend-react-spa)
14. [Testing Strategy](#14-testing-strategy)
15. [DevOps — Docker, CI/CD, Deployment](#15-devops--docker-cicd-deployment)
16. [Design Patterns Used](#16-design-patterns-used)
17. [Implementation Phases & Order](#17-implementation-phases--order)
18. [Configuration & Environment Variables](#18-configuration--environment-variables)
19. [Cost Summary](#19-cost-summary)

---

## 1. Tech Stack & Versions

### Backend

| Component | Technology | Version | Purpose |
|---|---|---|---|
| Language | Java (Eclipse Temurin) | 21 LTS | Long-term support, virtual threads, records, sealed classes |
| Framework | Spring Boot | 3.3.x | Core application framework |
| Web | Spring Web MVC | (bundled) | REST API controllers |
| Security | Spring Security | 6.x | Authentication, authorization, CORS |
| JWT | jjwt (io.jsonwebtoken) | 0.12.x | Token generation & validation |
| ORM | Spring Data JPA + Hibernate | (bundled) | Database access, repositories |
| Validation | Jakarta Bean Validation + Hibernate Validator | (bundled) | Input validation via annotations |
| Database | PostgreSQL | 16.x | Primary relational database |
| Cache | Redis + Spring Cache | 7.x | Flight search caching, OTP storage |
| Email | Spring Boot Mail Starter + SendGrid | (bundled) / 4.x | Transactional emails |
| SMS | Twilio SDK | 10.x | OTP via SMS |
| Flight Data | Amadeus Java SDK | 9.x | Real-time flight search with live pricing |
| PDF | OpenPDF | 2.0.x | E-ticket/boarding pass generation |
| QR Code | ZXing | 3.5.x | Boarding pass QR codes |
| API Docs | SpringDoc OpenAPI | 2.6.x | Swagger UI auto-generated docs |
| Boilerplate | Lombok | 1.18.x | @Builder, @Data, @Slf4j, etc. |
| Build Tool | Maven | 3.9.x | Dependency management, build lifecycle |
| Dev Tools | Spring Boot DevTools | (bundled) | Hot reload during development |

### Frontend

| Component | Technology | Version | Purpose |
|---|---|---|---|
| Framework | React | 18.x | Single-page application |
| Build Tool | Vite | 5.x | Fast bundling, HMR |
| Routing | React Router | 6.x | Client-side routing |
| HTTP Client | Axios | 1.x | API calls with interceptors for JWT |
| Styling | Tailwind CSS | 3.x | Utility-first styling |
| UI Components | shadcn/ui | latest | Pre-built accessible components |
| Forms | React Hook Form + Zod | 7.x / 3.x | Form handling + schema validation |
| State | React Context + useReducer | (built-in) | Auth state, booking flow state |
| Notifications | React Hot Toast | 2.x | Toast notifications |
| Date Picker | react-day-picker | 9.x | Flight date selection |
| Icons | Lucide React | latest | Consistent icon set |

### Infrastructure

| Component | Technology | Purpose |
|---|---|---|
| Containerization | Docker + Docker Compose | Local dev environment, production builds |
| CI/CD | GitHub Actions | Automated build, test, deploy |
| Backend Hosting | Render (free tier) | Spring Boot app hosting |
| Frontend Hosting | Vercel (free tier) | React SPA hosting |
| Database Hosting | Neon (free tier) | Managed PostgreSQL |
| Redis Hosting | Upstash (free tier) | Managed Redis |
| Image Registry | GitHub Container Registry | Docker image storage |

### Development Environment (M4 Pro MacBook Pro)

| Tool | Install Via | Notes |
|---|---|---|
| Java 21 | `brew install --cask temurin@21` | ARM64 native |
| Maven | `brew install maven` | Or use `./mvnw` wrapper |
| Node.js 20 LTS | `brew install node@20` | ARM64 native |
| PostgreSQL 16 | Docker (recommended) or `brew install postgresql@16` | |
| Redis 7 | Docker (recommended) or `brew install redis` | |
| Docker Desktop | `brew install --cask docker` | Apple Silicon native |
| IntelliJ IDEA CE or VS Code | `brew install --cask intellij-idea-ce` | IDE |

---

## 2. Project Structure

```
skywings-airways/
├── backend/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/skywings/
│   │   │   │   ├── SkywingsApplication.java              # @SpringBootApplication entry point
│   │   │   │   │
│   │   │   │   ├── config/
│   │   │   │   │   ├── SecurityConfig.java                # SecurityFilterChain, CORS, public/private routes
│   │   │   │   │   ├── JwtAuthenticationFilter.java       # OncePerRequestFilter — extracts & validates JWT
│   │   │   │   │   ├── RedisConfig.java                   # CacheManager, RedisTemplate beans
│   │   │   │   │   ├── OpenApiConfig.java                 # Swagger/OpenAPI metadata
│   │   │   │   │   └── AppConfig.java                     # PasswordEncoder bean, ObjectMapper, etc.
│   │   │   │   │
│   │   │   │   ├── entity/
│   │   │   │   │   ├── User.java                          # @Entity — id, name, email, passwordHash, role, phone
│   │   │   │   │   ├── Flight.java                        # @Entity — flightNumber, origin, destination, times, price
│   │   │   │   │   ├── Seat.java                          # @Entity — seatNumber, seatClass, flight FK, isAvailable
│   │   │   │   │   ├── Booking.java                       # @Entity — user FK, flight FK, status, totalPrice, payment FK
│   │   │   │   │   ├── BookingPassenger.java              # @Entity — booking FK, seat FK, name, age, passport
│   │   │   │   │   ├── Payment.java                       # @Entity — booking FK, amount, method, status, transactionId
│   │   │   │   │   └── enums/
│   │   │   │   │       ├── Role.java                      # ADMIN, PASSENGER
│   │   │   │   │       ├── SeatClass.java                 # ECONOMY, BUSINESS, FIRST
│   │   │   │   │       ├── BookingStatus.java             # PENDING, CONFIRMED, CANCELLED
│   │   │   │   │       ├── PaymentStatus.java             # PENDING, COMPLETED, REFUNDED
│   │   │   │   │       ├── PaymentMethod.java             # CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING
│   │   │   │   │       └── FlightStatus.java              # SCHEDULED, DELAYED, CANCELLED, COMPLETED
│   │   │   │   │
│   │   │   │   ├── dto/
│   │   │   │   │   ├── request/
│   │   │   │   │   │   ├── SignupRequest.java             # name, email, password, phone
│   │   │   │   │   │   ├── LoginRequest.java              # email, password
│   │   │   │   │   │   ├── FlightSearchRequest.java       # origin, destination, date, seatClass
│   │   │   │   │   │   ├── CreateFlightRequest.java       # all flight fields (admin)
│   │   │   │   │   │   ├── BookingRequest.java            # flightId, passengers[], seatClass
│   │   │   │   │   │   ├── PassengerDetail.java           # name, age, passportNumber (for each passenger)
│   │   │   │   │   │   ├── PaymentRequest.java            # cardNumber, expiryDate, cvv, paymentMethod
│   │   │   │   │   │   ├── OtpVerificationRequest.java    # bookingId, otp, channel (EMAIL/SMS)
│   │   │   │   │   │   └── UpdateFlightRequest.java       # partial flight update fields
│   │   │   │   │   │
│   │   │   │   │   └── response/
│   │   │   │   │       ├── AuthResponse.java              # token, refreshToken, user summary
│   │   │   │   │       ├── FlightResponse.java            # flight details + available seat counts
│   │   │   │   │       ├── BookingResponse.java           # booking summary, passengers, payment status
│   │   │   │   │       ├── PaymentResponse.java           # payment status, transactionId, otpSentVia
│   │   │   │   │       ├── SeatMapResponse.java           # list of seats with availability per class
│   │   │   │   │       ├── UserProfileResponse.java       # user details (no password)
│   │   │   │   │       ├── BookingHistoryResponse.java    # list of past bookings
│   │   │   │   │       ├── DashboardStatsResponse.java    # admin dashboard metrics
│   │   │   │   │       └── ApiErrorResponse.java          # timestamp, status, message, fieldErrors
│   │   │   │   │
│   │   │   │   ├── repository/
│   │   │   │   │   ├── UserRepository.java                # findByEmail, existsByEmail, existsByPhone
│   │   │   │   │   ├── FlightRepository.java              # searchFlights (custom JPQL), findByFlightNumber
│   │   │   │   │   ├── SeatRepository.java                # findByFlightAndSeatClass, countAvailable
│   │   │   │   │   ├── BookingRepository.java             # findByUser, findByUserAndStatus
│   │   │   │   │   ├── BookingPassengerRepository.java    # findByBooking
│   │   │   │   │   └── PaymentRepository.java             # findByBooking, findByTransactionId
│   │   │   │   │
│   │   │   │   ├── service/
│   │   │   │   │   ├── AuthService.java                   # signup, login, token refresh
│   │   │   │   │   ├── JwtService.java                    # generateToken, validateToken, extractClaims
│   │   │   │   │   ├── FlightService.java                 # search, create, update, delete, getSeatMap
│   │   │   │   │   ├── AmadeusFlightService.java          # real-time flight search via Amadeus API (fallback: DB)
│   │   │   │   │   ├── BookingService.java                # createBooking, cancelBooking, getBookings, viewBill
│   │   │   │   │   ├── PaymentService.java                # initiatePayment, verifyOtp, processRefund
│   │   │   │   │   ├── OtpService.java                    # generateOtp, sendViaEmail, sendViaSms, verify
│   │   │   │   │   ├── NotificationService.java           # booking confirmation, cancellation, payment receipt emails
│   │   │   │   │   ├── TicketService.java                 # generateETicketPdf, generateBoardingPass, generateQrCode
│   │   │   │   │   ├── PricingService.java                # calculateFare, calculateTax, calculatePenalty
│   │   │   │   │   ├── AdminService.java                  # dashboard stats, manage flights, view all bookings/users
│   │   │   │   │   └── UserService.java                   # getProfile, updateProfile
│   │   │   │   │
│   │   │   │   ├── controller/
│   │   │   │   │   ├── AuthController.java                # POST /api/auth/signup, /login, /refresh
│   │   │   │   │   ├── FlightController.java              # GET /api/flights/search, GET /api/flights/{id}/seats
│   │   │   │   │   ├── BookingController.java             # POST /api/bookings, DELETE, GET history
│   │   │   │   │   ├── PaymentController.java             # POST /api/payments/initiate, /verify-otp
│   │   │   │   │   ├── TicketController.java              # GET /api/tickets/{bookingId}/download
│   │   │   │   │   ├── UserController.java                # GET/PUT /api/users/profile
│   │   │   │   │   └── AdminController.java               # All admin CRUD operations
│   │   │   │   │
│   │   │   │   ├── exception/
│   │   │   │   │   ├── GlobalExceptionHandler.java        # @RestControllerAdvice — unified error responses
│   │   │   │   │   ├── ResourceNotFoundException.java     # 404
│   │   │   │   │   ├── DuplicateResourceException.java    # 409
│   │   │   │   │   ├── InsufficientSeatsException.java    # 409
│   │   │   │   │   ├── InvalidPaymentException.java       # 400
│   │   │   │   │   ├── OtpVerificationException.java      # 400
│   │   │   │   │   ├── BookingNotCancellableException.java # 400
│   │   │   │   │   └── UnauthorizedException.java         # 401
│   │   │   │   │
│   │   │   │   └── util/
│   │   │   │       ├── LuhnValidator.java                 # Credit card number validation
│   │   │   │       └── TransactionIdGenerator.java        # Generate unique transaction IDs (SKY-xxxx-xxxx)
│   │   │   │
│   │   │   └── resources/
│   │   │       ├── application.yml                        # Main config (DB, Redis, JWT, mail)
│   │   │       ├── application-dev.yml                    # Dev profile overrides
│   │   │       ├── application-prod.yml                   # Prod profile overrides
│   │   │       ├── templates/
│   │   │       │   ├── booking-confirmation.html          # Thymeleaf email template
│   │   │       │   ├── cancellation-notice.html           # Thymeleaf email template
│   │   │       │   ├── payment-receipt.html               # Thymeleaf email template
│   │   │       │   └── otp-email.html                     # Thymeleaf email template for OTP
│   │   │       └── data.sql                               # Seed data (sample flights, admin account)
│   │   │
│   │   └── test/
│   │       └── java/com/skywings/
│   │           ├── service/
│   │           │   ├── AuthServiceTest.java
│   │           │   ├── BookingServiceTest.java
│   │           │   ├── PricingServiceTest.java
│   │           │   ├── OtpServiceTest.java
│   │           │   └── PaymentServiceTest.java
│   │           ├── controller/
│   │           │   ├── AuthControllerTest.java
│   │           │   ├── FlightControllerTest.java
│   │           │   └── BookingControllerTest.java
│   │           ├── repository/
│   │           │   ├── FlightRepositoryTest.java
│   │           │   └── BookingRepositoryTest.java
│   │           └── integration/
│   │               ├── BookingFlowIntegrationTest.java
│   │               └── PaymentFlowIntegrationTest.java
│   │
│   └── Dockerfile
│
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── tailwind.config.js
│   ├── index.html
│   ├── public/
│   │   └── skywings-logo.svg
│   ├── src/
│   │   ├── main.jsx
│   │   ├── App.jsx                                        # Router setup, auth provider wrapper
│   │   │
│   │   ├── api/
│   │   │   └── axios.js                                   # Axios instance with baseURL, JWT interceptor
│   │   │
│   │   ├── context/
│   │   │   └── AuthContext.jsx                            # Login state, token storage, logout
│   │   │
│   │   ├── hooks/
│   │   │   ├── useAuth.js                                 # Access auth context
│   │   │   ├── useFlightSearch.js                         # Flight search + caching
│   │   │   └── useBooking.js                              # Booking flow state machine
│   │   │
│   │   ├── components/
│   │   │   ├── layout/
│   │   │   │   ├── Navbar.jsx                             # Top nav with auth-aware links
│   │   │   │   ├── Footer.jsx
│   │   │   │   └── ProtectedRoute.jsx                     # Redirect if not authenticated
│   │   │   │
│   │   │   ├── flights/
│   │   │   │   ├── FlightSearchForm.jsx                   # Origin, destination, date, class picker
│   │   │   │   ├── FlightCard.jsx                         # Individual flight result display
│   │   │   │   ├── FlightList.jsx                         # Search results list with sorting
│   │   │   │   └── SeatMap.jsx                            # Visual seat layout (grid-based)
│   │   │   │
│   │   │   ├── booking/
│   │   │   │   ├── BookingStepper.jsx                     # Multi-step progress indicator
│   │   │   │   ├── PassengerForm.jsx                      # Passenger details entry
│   │   │   │   ├── BookingSummary.jsx                     # Review before payment
│   │   │   │   └── BookingConfirmation.jsx                # Success page with download link
│   │   │   │
│   │   │   ├── payment/
│   │   │   │   ├── PaymentForm.jsx                        # Card number, expiry, CVV fields
│   │   │   │   ├── OtpVerification.jsx                    # OTP input with countdown timer
│   │   │   │   └── PaymentMethodSelector.jsx              # Card / UPI / Net Banking tabs
│   │   │   │
│   │   │   └── admin/
│   │   │       ├── FlightForm.jsx                         # Add/edit flight form
│   │   │       ├── FlightTable.jsx                        # All flights with actions
│   │   │       ├── BookingTable.jsx                       # All bookings management
│   │   │       ├── UserTable.jsx                          # Registered users list
│   │   │       └── StatsCards.jsx                         # Dashboard metrics cards
│   │   │
│   │   └── pages/
│   │       ├── HomePage.jsx                               # Landing page with search
│   │       ├── LoginPage.jsx
│   │       ├── SignupPage.jsx
│   │       ├── FlightResultsPage.jsx                      # Search results
│   │       ├── BookingPage.jsx                            # Multi-step booking flow
│   │       ├── PaymentPage.jsx                            # Payment + OTP
│   │       ├── MyBookingsPage.jsx                         # Booking history
│   │       ├── ProfilePage.jsx                            # User profile
│   │       ├── AboutPage.jsx                              # Airline info
│   │       └── admin/
│   │           ├── AdminDashboardPage.jsx                 # Stats overview
│   │           ├── ManageFlightsPage.jsx                  # CRUD flights
│   │           ├── ManageBookingsPage.jsx                 # View/manage bookings
│   │           └── ManageUsersPage.jsx                    # View registered users
│   │
│   └── Dockerfile
│
├── docker-compose.yml                                     # App + PostgreSQL + Redis
├── .github/
│   └── workflows/
│       └── ci-cd.yml                                      # Build, test, deploy pipeline
├── .env.example                                           # Template for environment variables
├── IMPLEMENTATION_PLAN.md                                 # This file
└── README.md                                              # Project overview (generated at end)
```

---

## 3. Database Schema

### ER Diagram (Textual)

```
┌──────────┐       ┌───────────┐       ┌──────────┐
│  users   │       │  flights  │       │  seats   │
├──────────┤       ├───────────┤       ├──────────┤
│ id (PK)  │       │ id (PK)   │       │ id (PK)  │
│ name     │       │ flight_no │       │ flight_id│───→ flights.id
│ email    │       │ airline   │       │ seat_no  │
│ password │       │ origin    │       │ seat_cls │
│ phone    │       │ destinatn │       │ is_avail │
│ role     │       │ dept_time │       │ price    │
│ created  │       │ arrv_time │       └──────────┘
│ updated  │       │ status    │
└──────────┘       │ base_price│
     │             │ created   │
     │             └───────────┘
     │                   │
     ▼                   ▼
┌──────────────┐   ┌────────────────────┐   ┌────────────┐
│   bookings   │   │ booking_passengers │   │  payments  │
├──────────────┤   ├────────────────────┤   ├────────────┤
│ id (PK)      │   │ id (PK)            │   │ id (PK)    │
│ user_id (FK) │   │ booking_id (FK)    │───→ bookings.id
│ flight_id(FK)│   │ seat_id (FK)       │───→ seats.id
│ booking_date │   │ passenger_name     │   │ booking_id │───→ bookings.id
│ status       │   │ age                │   │ amount     │
│ total_price  │   │ passport_number    │   │ method     │
│ seat_class   │   └────────────────────┘   │ status     │
│ num_seats    │                             │ txn_id     │
│ penalty_amt  │                             │ card_last4 │
│ version (OL) │                             │ created    │
│ created      │                             └────────────┘
│ updated      │
└──────────────┘
```

### Full DDL

```sql
-- Users table
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    phone           VARCHAR(15) NOT NULL UNIQUE,
    role            VARCHAR(20) NOT NULL DEFAULT 'PASSENGER',  -- PASSENGER, ADMIN
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);

-- Flights table
CREATE TABLE flights (
    id              BIGSERIAL PRIMARY KEY,
    flight_number   VARCHAR(10) NOT NULL UNIQUE,       -- e.g., SW-101
    airline         VARCHAR(100) NOT NULL DEFAULT 'SkyWings Airways',
    origin          VARCHAR(100) NOT NULL,              -- e.g., Delhi (DEL)
    destination     VARCHAR(100) NOT NULL,              -- e.g., Mumbai (BOM)
    origin_code     VARCHAR(5) NOT NULL,                -- IATA code: DEL
    dest_code       VARCHAR(5) NOT NULL,                -- IATA code: BOM
    departure_time  TIMESTAMP NOT NULL,
    arrival_time    TIMESTAMP NOT NULL,
    flight_type     VARCHAR(15) NOT NULL,               -- DOMESTIC, INTERNATIONAL
    status          VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    base_price_economy   DECIMAL(10,2) NOT NULL,
    base_price_business  DECIMAL(10,2) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_flights_route ON flights(origin_code, dest_code);
CREATE INDEX idx_flights_departure ON flights(departure_time);

-- Seats table
CREATE TABLE seats (
    id              BIGSERIAL PRIMARY KEY,
    flight_id       BIGINT NOT NULL REFERENCES flights(id) ON DELETE CASCADE,
    seat_number     VARCHAR(5) NOT NULL,                -- e.g., 1A, 12F
    seat_class      VARCHAR(20) NOT NULL,               -- ECONOMY, BUSINESS
    is_available    BOOLEAN NOT NULL DEFAULT TRUE,
    price           DECIMAL(10,2) NOT NULL,
    UNIQUE(flight_id, seat_number)
);

CREATE INDEX idx_seats_flight ON seats(flight_id);
CREATE INDEX idx_seats_availability ON seats(flight_id, seat_class, is_available);

-- Bookings table
CREATE TABLE bookings (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id),
    flight_id       BIGINT NOT NULL REFERENCES flights(id),
    booking_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, CONFIRMED, CANCELLED
    seat_class      VARCHAR(20) NOT NULL,
    num_seats       INT NOT NULL,
    total_price     DECIMAL(10,2) NOT NULL,
    tax_amount      DECIMAL(10,2) NOT NULL,
    penalty_amount  DECIMAL(10,2) NOT NULL DEFAULT 0,
    version         INT NOT NULL DEFAULT 0,            -- Optimistic locking
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bookings_user ON bookings(user_id);
CREATE INDEX idx_bookings_flight ON bookings(flight_id);

-- Booking passengers (one booking can have multiple passengers)
CREATE TABLE booking_passengers (
    id              BIGSERIAL PRIMARY KEY,
    booking_id      BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    seat_id         BIGINT REFERENCES seats(id),
    passenger_name  VARCHAR(100) NOT NULL,
    age             INT NOT NULL,
    passport_number VARCHAR(20),                       -- Required for INTERNATIONAL flights only
    UNIQUE(booking_id, seat_id)
);

-- Payments table
CREATE TABLE payments (
    id              BIGSERIAL PRIMARY KEY,
    booking_id      BIGINT NOT NULL REFERENCES bookings(id),
    amount          DECIMAL(10,2) NOT NULL,
    payment_method  VARCHAR(30) NOT NULL,              -- CREDIT_CARD, DEBIT_CARD, UPI, NET_BANKING
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING',  -- PENDING, COMPLETED, REFUNDED
    transaction_id  VARCHAR(50) NOT NULL UNIQUE,       -- SKY-xxxx-xxxx format
    card_last_four  VARCHAR(4),                        -- Last 4 digits for display
    otp_verified    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_payments_booking ON payments(booking_id);
CREATE INDEX idx_payments_txn ON payments(transaction_id);
```

### Seed Data (data.sql)

Insert on first run:
- 1 admin account (email: `admin@skywings.com`, password: BCrypt-hashed)
- 15-20 sample flights (mix of domestic and international)
- Seats auto-generated per flight: 30 economy + 10 business = 40 seats per flight
- Seat numbering: Business (rows 1-2, seats A-E → 1A-2E), Economy (rows 3-8, seats A-E → 3A-8E)

---

## 4. Entity Design & Relationships

### User Entity

```java
@Entity
@Table(name = "users")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class User implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() { createdAt = updatedAt = LocalDateTime.now(); }

    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    // UserDetails interface methods:
    // getAuthorities() → returns role as GrantedAuthority
    // getUsername() → returns email
    // getPassword() → returns passwordHash
    // isAccountNonExpired/Locked/CredentialsNonExpired/Enabled → all return true
}
```

### Flight Entity

```java
@Entity
@Table(name = "flights")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Flight {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flight_number", nullable = false, unique = true, length = 10)
    private String flightNumber;

    @Column(nullable = false, length = 100)
    private String airline;

    @Column(nullable = false, length = 100)
    private String origin;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(name = "origin_code", nullable = false, length = 5)
    private String originCode;

    @Column(name = "dest_code", nullable = false, length = 5)
    private String destCode;

    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;

    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;

    @Column(name = "flight_type", nullable = false, length = 15)
    private String flightType;  // DOMESTIC, INTERNATIONAL

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FlightStatus status;

    @Column(name = "base_price_economy", nullable = false)
    private BigDecimal basePriceEconomy;

    @Column(name = "base_price_business", nullable = false)
    private BigDecimal basePriceBusiness;

    @OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Seat> seats;
}
```

### Booking Entity (with Optimistic Locking)

```java
@Entity
@Table(name = "bookings")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Booking {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flight_id", nullable = false)
    private Flight flight;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_class", nullable = false, length = 20)
    private SeatClass seatClass;

    @Column(name = "num_seats", nullable = false)
    private Integer numSeats;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "tax_amount", nullable = false)
    private BigDecimal taxAmount;

    @Column(name = "penalty_amount", nullable = false)
    private BigDecimal penaltyAmount;

    @Version  // Optimistic locking — prevents double-booking race conditions
    private Integer version;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    private List<BookingPassenger> passengers;

    @OneToOne(mappedBy = "booking", cascade = CascadeType.ALL)
    private Payment payment;
}
```

### Relationships Summary

```
User (1) ──→ (N) Booking
Flight (1) ──→ (N) Seat
Flight (1) ──→ (N) Booking
Booking (1) ──→ (N) BookingPassenger
Booking (1) ──→ (1) Payment
BookingPassenger (N) ──→ (1) Seat
```

---

## 5. API Design — All Endpoints

### Authentication (`/api/auth`)

| Method | Endpoint | Request Body | Response | Auth |
|---|---|---|---|---|
| POST | `/api/auth/signup` | `SignupRequest` | `AuthResponse` (201) | Public |
| POST | `/api/auth/login` | `LoginRequest` | `AuthResponse` (200) | Public |
| POST | `/api/auth/refresh` | `{ refreshToken }` | `AuthResponse` (200) | Public |

### Flights (`/api/flights`)

| Method | Endpoint | Params/Body | Response | Auth |
|---|---|---|---|---|
| GET | `/api/flights/search` | `?origin=DEL&dest=BOM&date=2026-04-15&seatClass=ECONOMY` | `List<FlightResponse>` (200) | Public |
| GET | `/api/flights/{id}` | — | `FlightResponse` (200) | Public |
| GET | `/api/flights/{id}/seats` | `?seatClass=ECONOMY` | `SeatMapResponse` (200) | Authenticated |
| POST | `/api/flights` | `CreateFlightRequest` | `FlightResponse` (201) | ADMIN |
| PUT | `/api/flights/{id}` | `UpdateFlightRequest` | `FlightResponse` (200) | ADMIN |
| DELETE | `/api/flights/{id}` | — | 204 | ADMIN |
| GET | `/api/flights/live-search` | `?origin=DEL&dest=BOM&date=2026-04-15&adults=1` | `List<AmadeusFlightResponse>` (200) | Public |

### Bookings (`/api/bookings`)

| Method | Endpoint | Request Body | Response | Auth |
|---|---|---|---|---|
| POST | `/api/bookings` | `BookingRequest` | `BookingResponse` (201) | PASSENGER |
| GET | `/api/bookings` | — | `List<BookingResponse>` (200) | PASSENGER (own) |
| GET | `/api/bookings/{id}` | — | `BookingResponse` (200) | PASSENGER (own) / ADMIN |
| DELETE | `/api/bookings/{id}` | — | `BookingResponse` with penalty (200) | PASSENGER (own) |
| GET | `/api/bookings/{id}/bill` | — | `BillResponse` (200) | PASSENGER (own) |

### Payments (`/api/payments`)

| Method | Endpoint | Request Body | Response | Auth |
|---|---|---|---|---|
| POST | `/api/payments/initiate` | `PaymentRequest` | `PaymentResponse` (200) | PASSENGER |
| POST | `/api/payments/verify-otp` | `OtpVerificationRequest` | `PaymentResponse` (200) | PASSENGER |
| POST | `/api/payments/resend-otp` | `{ bookingId, channel }` | `{ message, channel }` (200) | PASSENGER |

### Tickets (`/api/tickets`)

| Method | Endpoint | Response | Auth |
|---|---|---|---|
| GET | `/api/tickets/{bookingId}/eticket` | PDF file download | PASSENGER (own) |
| GET | `/api/tickets/{bookingId}/boarding-pass` | PDF file download | PASSENGER (own) |

### Users (`/api/users`)

| Method | Endpoint | Request Body | Response | Auth |
|---|---|---|---|---|
| GET | `/api/users/profile` | — | `UserProfileResponse` (200) | Authenticated |
| PUT | `/api/users/profile` | `UpdateProfileRequest` | `UserProfileResponse` (200) | Authenticated |
| PUT | `/api/users/change-password` | `ChangePasswordRequest` | 200 | Authenticated |

### Admin (`/api/admin`)

| Method | Endpoint | Response | Auth |
|---|---|---|---|
| GET | `/api/admin/dashboard` | `DashboardStatsResponse` (200) | ADMIN |
| GET | `/api/admin/bookings` | `List<BookingResponse>` (200) | ADMIN |
| GET | `/api/admin/bookings?status=CONFIRMED` | Filtered bookings (200) | ADMIN |
| GET | `/api/admin/users` | `List<UserProfileResponse>` (200) | ADMIN |
| GET | `/api/admin/users/{id}` | `UserProfileResponse` (200) | ADMIN |

### API Error Response Format (all errors)

```json
{
    "timestamp": "2026-04-01T10:30:00",
    "status": 400,
    "error": "Bad Request",
    "message": "Validation failed",
    "fieldErrors": [
        { "field": "email", "message": "must be a valid email address" },
        { "field": "password", "message": "must be at least 8 characters" }
    ]
}
```

---

## 6. Authentication & Authorization

### Flow

```
1. User signs up → password hashed with BCrypt(strength=12) → saved to DB
2. User logs in → credentials validated → JWT access token (30 min) + refresh token (7 days) issued
3. Client stores tokens in memory (access) and httpOnly cookie or localStorage (refresh)
4. Every API request includes: Authorization: Bearer <access_token>
5. JwtAuthenticationFilter intercepts requests → validates token → sets SecurityContext
6. When access token expires → client calls /api/auth/refresh with refresh token → new pair issued
```

### SecurityConfig.java — Key Configuration

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(csrf -> csrf.disable())  // Stateless API — CSRF not needed
        .cors(cors -> cors.configurationSource(corsConfigSource()))
        .sessionManagement(sm -> sm.sessionPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers("/api/flights/search", "/api/flights/{id}").permitAll()
            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
}
```

### JWT Structure

```json
{
    "sub": "user@example.com",
    "userId": 42,
    "role": "PASSENGER",
    "iat": 1711929600,
    "exp": 1711931400
}
```

### Password Rules (validated at signup)

- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 digit
- At least 1 special character (`@$!%*?&#`)
- Validated via `@Pattern` annotation on `SignupRequest.password`

```java
@Pattern(
    regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,}$",
    message = "Password must contain at least 8 characters, one uppercase, one lowercase, one digit, and one special character"
)
private String password;
```

---

## 7. Core Business Logic

### 7.1 Flight Search

```
Input: origin (code), destination (code), date, seatClass (optional)
Process:
  1. Query flights matching origin + destination + date (date = departure date)
  2. Filter by status = SCHEDULED only
  3. For each flight, count available seats per class from seats table
  4. Sort by departure time (default), allow sort by price
  5. Cache results in Redis with key "flights:{origin}:{dest}:{date}" — TTL 5 minutes
Output: List of FlightResponse with available seat counts and prices
```

**FlightRepository custom query:**
```java
@Query("SELECT f FROM Flight f WHERE f.originCode = :origin AND f.destCode = :dest " +
       "AND DATE(f.departureTime) = :date AND f.status = 'SCHEDULED' ORDER BY f.departureTime")
List<Flight> searchFlights(@Param("origin") String origin,
                           @Param("dest") String dest,
                           @Param("date") LocalDate date);
```

### 7.2 Booking Flow (Step by Step)

```
Step 1: User selects flight + seat class + number of passengers
          → POST /api/bookings with BookingRequest
          → Backend validates: flight exists, enough available seats, flight not departed

Step 2: Backend assigns seats (first-available in requested class)
          → Uses @Lock(PESSIMISTIC_WRITE) on seat query to prevent race conditions
          → Marks selected seats as is_available = false
          → Creates Booking (status = PENDING) + BookingPassenger entries
          → Calculates price via PricingService

Step 3: User proceeds to payment
          → POST /api/payments/initiate with card details
          → Backend validates card (Luhn check), stores last 4 digits
          → Sends OTP via both email AND SMS
          → Returns PaymentResponse with otpSentVia: ["EMAIL", "SMS"]

Step 4: User enters OTP
          → POST /api/payments/verify-otp
          → Backend verifies OTP from Redis
          → If valid: Payment status → COMPLETED, Booking status → CONFIRMED
          → Sends confirmation email with e-ticket PDF attachment

If booking not paid within 15 minutes → scheduled task releases seats, cancels booking
```

### 7.3 Seat Assignment Logic

```java
// In BookingService.createBooking():
@Transactional
public BookingResponse createBooking(BookingRequest request, User user) {
    Flight flight = flightRepository.findById(request.getFlightId())
        .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

    // Pessimistic lock — blocks other transactions from grabbing same seats
    List<Seat> availableSeats = seatRepository.findAvailableSeatsByFlightAndClass(
        flight.getId(), request.getSeatClass(), LockModeType.PESSIMISTIC_WRITE);

    if (availableSeats.size() < request.getPassengers().size()) {
        throw new InsufficientSeatsException("Only " + availableSeats.size() + " seats available");
    }

    // Assign seats to passengers (first N available)
    List<Seat> assignedSeats = availableSeats.subList(0, request.getPassengers().size());
    assignedSeats.forEach(seat -> seat.setIsAvailable(false));
    seatRepository.saveAll(assignedSeats);

    // Calculate pricing
    PricingResult pricing = pricingService.calculate(flight, request.getSeatClass(),
                                                      request.getPassengers().size());

    // Create booking
    Booking booking = Booking.builder()
        .user(user).flight(flight).bookingDate(LocalDateTime.now())
        .status(BookingStatus.PENDING).seatClass(request.getSeatClass())
        .numSeats(request.getPassengers().size())
        .totalPrice(pricing.total()).taxAmount(pricing.tax())
        .penaltyAmount(BigDecimal.ZERO)
        .build();
    booking = bookingRepository.save(booking);

    // Create passenger entries with seat assignments
    for (int i = 0; i < request.getPassengers().size(); i++) {
        PassengerDetail pd = request.getPassengers().get(i);
        BookingPassenger bp = BookingPassenger.builder()
            .booking(booking).seat(assignedSeats.get(i))
            .passengerName(pd.getName()).age(pd.getAge())
            .passportNumber(pd.getPassportNumber())
            .build();
        bookingPassengerRepository.save(bp);
    }

    // Evict flight search cache (seat availability changed)
    cacheManager.getCache("flights").evict(flight.getOriginCode() + ":" +
        flight.getDestCode() + ":" + flight.getDepartureTime().toLocalDate());

    return toBookingResponse(booking);
}
```

### 7.4 Pricing Service

```java
@Service
public class PricingService {

    // Tax rates
    private static final BigDecimal DOMESTIC_TAX_RATE = new BigDecimal("0.05");       // 5%
    private static final BigDecimal INTERNATIONAL_TAX_RATE = new BigDecimal("0.08");  // 8%

    // Cancellation penalty
    private static final BigDecimal CANCELLATION_PENALTY_RATE = new BigDecimal("0.25"); // 25%

    public PricingResult calculate(Flight flight, SeatClass seatClass, int numSeats) {
        BigDecimal basePrice = switch (seatClass) {
            case ECONOMY -> flight.getBasePriceEconomy();
            case BUSINESS -> flight.getBasePriceBusiness();
        };

        // Duration-based multiplier: price * duration in hours
        long durationMinutes = Duration.between(flight.getDepartureTime(),
                                                 flight.getArrivalTime()).toMinutes();
        BigDecimal durationHours = new BigDecimal(durationMinutes)
                                       .divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);

        BigDecimal subtotal = basePrice.multiply(durationHours)
                                       .multiply(BigDecimal.valueOf(numSeats));

        BigDecimal taxRate = flight.getFlightType().equals("INTERNATIONAL")
                             ? INTERNATIONAL_TAX_RATE : DOMESTIC_TAX_RATE;

        BigDecimal tax = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(tax);

        return new PricingResult(subtotal, tax, total);
    }

    public BigDecimal calculatePenalty(BigDecimal totalPrice) {
        return totalPrice.multiply(CANCELLATION_PENALTY_RATE)
                         .setScale(2, RoundingMode.HALF_UP);
    }
}
```

**PricingResult (Java Record):**
```java
public record PricingResult(BigDecimal subtotal, BigDecimal tax, BigDecimal total) {}
```

### 7.5 Cancellation Flow

```
1. User calls DELETE /api/bookings/{id}
2. Validate: booking belongs to user, status is CONFIRMED, flight hasn't departed
3. Calculate penalty (25% of total price)
4. Update booking: status → CANCELLED, penalty_amount set
5. Release seats: mark assigned seats as is_available = true
6. Update payment: status → REFUNDED
7. Send cancellation email with penalty details
8. Evict flight cache
9. Return updated BookingResponse with penalty breakdown
```

### 7.6 Booking Expiry (Scheduled Task)

```java
@Scheduled(fixedRate = 60000)  // Runs every 1 minute
@Transactional
public void expireUnpaidBookings() {
    LocalDateTime cutoff = LocalDateTime.now().minusMinutes(15);
    List<Booking> expired = bookingRepository
        .findByStatusAndCreatedAtBefore(BookingStatus.PENDING, cutoff);

    for (Booking booking : expired) {
        booking.setStatus(BookingStatus.CANCELLED);
        // Release seats
        booking.getPassengers().forEach(bp -> {
            bp.getSeat().setIsAvailable(true);
            seatRepository.save(bp.getSeat());
        });
        bookingRepository.save(booking);
    }
}
```

---

## 8. Real-Time Flight Data — Amadeus API Integration

### Overview

The system uses a **hybrid approach** for flight data:
- **Primary source**: Amadeus Self-Service API — returns real flights from real airlines (Air India, IndiGo, Vistara, Emirates, etc.) with live pricing
- **Fallback source**: SkyWings database (seeded flights) — used when Amadeus is unavailable, rate-limited, or for admin CRUD demo features

This means users see **real airline data with real prices** when searching, while the rest of the system (booking, payment, OTP, tickets) continues to work with our internal data model.

### Amadeus Self-Service API

| Detail | Info |
|---|---|
| **Provider** | Amadeus for Developers (amadeus.com/en/developers) |
| **Free tier** | 2,000 API calls/month (no credit card required) |
| **Environment** | Test (cached real data) → Production (live data, requires approval) |
| **Java SDK** | `com.amadeus:amadeus-java:9.1.0` (official, Maven Central) |
| **Auth** | API Key + API Secret → auto-generates OAuth2 token |
| **Key endpoint** | `GET /v2/shopping/flight-offers` |
| **Data returned** | Airlines, flight numbers, departure/arrival times, prices, stops, duration, cabin class |

### How It Works

```
User searches: "DEL → BOM, 15 Apr 2026, 1 adult"
        ↓
FlightController.liveSearch()
        ↓
AmadeusFlightService.searchFlights()
        ↓
    ┌────────────────────────────────┐
    │ 1. Call Amadeus API            │
    │    GET /v2/shopping/flight-    │
    │    offers?originLocationCode=  │
    │    DEL&destinationLocation     │
    │    Code=BOM&departureDate=     │
    │    2026-04-15&adults=1         │
    │                                │
    │ 2. Parse response → map to     │
    │    AmadeusFlightResponse DTOs  │
    │                                │
    │ 3. Cache results in Redis      │
    │    (key: "amadeus:DEL:BOM:     │
    │    2026-04-15", TTL: 5 min)    │
    │                                │
    │ 4. If API fails → fall back    │
    │    to DB search (seed flights) │
    └────────────────────────────────┘
        ↓
Returns: List of real flights with real prices
```

### Hybrid Architecture — What Goes Where

| Feature | Uses Amadeus (real data) | Uses SkyWings DB (internal) |
|---|---|---|
| Flight search results | Yes — real airlines, prices, times | Fallback only |
| Flight details page | Yes — data from search is cached | Fallback only |
| Admin CRUD flights | No | Yes — admin manages SkyWings fleet |
| Seat map & selection | No | Yes — seats are internal to our system |
| Booking creation | No | Yes — booking references internal flight record |
| Pricing | Amadeus price shown in search | Internal PricingService used for booking |
| Payment, OTP, tickets | No | Yes — fully internal |

**Key design decision**: When a user selects an Amadeus flight to book, we create a corresponding internal Flight record in our database (if it doesn't already exist) so that the booking, seat, and payment system can reference it. This keeps the downstream flow unchanged.

### AmadeusFlightService Implementation

```java
@Service
@Slf4j
public class AmadeusFlightService {

    private Amadeus amadeus;

    @Value("${amadeus.api-key}")
    private String apiKey;

    @Value("${amadeus.api-secret}")
    private String apiSecret;

    @Value("${amadeus.environment:test}")
    private String environment;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        try {
            if (!"placeholder".equals(apiKey)) {
                Amadeus.Configuration config = Amadeus.builder(apiKey, apiSecret);
                if ("production".equals(environment)) {
                    config.setHostname(Hostname.PRODUCTION);
                }
                amadeus = config.build();
                log.info("Amadeus API initialized (env: {})", environment);
            } else {
                log.warn("Amadeus credentials not configured. Live flight search disabled.");
            }
        } catch (Exception e) {
            log.warn("Amadeus initialization failed: {}", e.getMessage());
        }
    }

    public List<AmadeusFlightResponse> searchFlights(String origin, String dest,
                                                       LocalDate date, int adults) {
        if (amadeus == null) {
            return List.of(); // Fallback to DB search in controller
        }

        // Check Redis cache first
        String cacheKey = "amadeus:" + origin + ":" + dest + ":" + date;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return deserializeFromCache(cached);
        }

        try {
            FlightOfferSearch[] offers = amadeus.shopping.flightOffersSearch.get(
                Params.with("originLocationCode", origin.toUpperCase())
                      .and("destinationLocationCode", dest.toUpperCase())
                      .and("departureDate", date.toString())
                      .and("adults", adults)
                      .and("max", 20)                    // Limit to 20 results
                      .and("currencyCode", "INR")
            );

            List<AmadeusFlightResponse> results = Arrays.stream(offers)
                .map(this::mapToResponse)
                .toList();

            // Cache for 5 minutes
            redisTemplate.opsForValue().set(cacheKey,
                objectMapper.writeValueAsString(results), 5, TimeUnit.MINUTES);

            return results;

        } catch (ResponseException e) {
            log.error("Amadeus API error: {} — {}", e.getCode(), e.getMessage());
            return List.of(); // Fallback to DB
        }
    }

    private AmadeusFlightResponse mapToResponse(FlightOfferSearch offer) {
        FlightOfferSearch.SearchSegment segment =
            offer.getItineraries()[0].getSegments()[0];
        FlightOfferSearch.SearchSegment lastSegment =
            offer.getItineraries()[0].getSegments()[
                offer.getItineraries()[0].getSegments().length - 1];

        return AmadeusFlightResponse.builder()
            .amadeusOfferId(offer.getId())
            .airline(segment.getCarrierCode())
            .flightNumber(segment.getCarrierCode() + "-" + segment.getNumber())
            .origin(segment.getDeparture().getIataCode())
            .destination(lastSegment.getArrival().getIataCode())
            .departureTime(segment.getDeparture().getAt())
            .arrivalTime(lastSegment.getArrival().getAt())
            .duration(offer.getItineraries()[0].getDuration())   // e.g., "PT2H15M"
            .stops(offer.getItineraries()[0].getSegments().length - 1)
            .price(new BigDecimal(offer.getPrice().getTotal()))
            .currency(offer.getPrice().getCurrency())
            .cabin(offer.getTravelerPricings()[0].getFareDetailsBySegment()[0].getCabin())
            .seatsAvailable(offer.getNumberOfBookableSeats())
            .source("AMADEUS")
            .build();
    }
}
```

### AmadeusFlightResponse DTO

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AmadeusFlightResponse {

    private String amadeusOfferId;        // Amadeus-assigned offer ID
    private String airline;               // Carrier code: "AI", "6E", "UK"
    private String flightNumber;          // "AI-101", "6E-302"
    private String origin;                // IATA code: "DEL"
    private String destination;           // IATA code: "BOM"
    private String departureTime;         // ISO datetime
    private String arrivalTime;           // ISO datetime
    private String duration;              // ISO 8601 duration: "PT2H15M"
    private int stops;                    // 0 = direct, 1 = one stop, etc.
    private BigDecimal price;             // Total price
    private String currency;              // "INR", "USD"
    private String cabin;                 // "ECONOMY", "BUSINESS"
    private int seatsAvailable;           // Bookable seats
    private String source;                // "AMADEUS" (distinguishes from DB flights)
}
```

### FlightController — Live Search Endpoint

```java
@GetMapping("/live-search")
public ResponseEntity<List<AmadeusFlightResponse>> liveSearchFlights(
        @RequestParam String origin,
        @RequestParam String dest,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
        @RequestParam(defaultValue = "1") int adults) {

    List<AmadeusFlightResponse> results = amadeusFlightService.searchFlights(
        origin, dest, date, adults);

    // If Amadeus returns empty (not configured or error), fall back to DB
    if (results.isEmpty()) {
        List<FlightResponse> dbFlights = flightService.searchFlights(origin, dest, date);
        // Map DB flights to AmadeusFlightResponse format for consistent frontend
        results = dbFlights.stream()
            .map(f -> AmadeusFlightResponse.builder()
                .flightNumber(f.getFlightNumber())
                .airline("SkyWings Airways")
                .origin(f.getOriginCode())
                .destination(f.getDestCode())
                .departureTime(f.getDepartureTime().toString())
                .arrivalTime(f.getArrivalTime().toString())
                .duration(f.getDuration())
                .stops(0)
                .price(f.getBasePriceEconomy())
                .currency("INR")
                .cabin("ECONOMY")
                .seatsAvailable(f.getAvailableEconomySeats().intValue())
                .source("SKYWINGS_DB")
                .build())
            .toList();
    }

    return ResponseEntity.ok(results);
}
```

### Amadeus Setup Steps (for developer)

```
1. Go to https://developers.amadeus.com
2. Create a free account (no credit card needed)
3. Go to "My Self-Service Workspace" → "Create New App"
4. Copy the API Key and API Secret
5. Add to .env or application.yml:
     amadeus.api-key=YOUR_API_KEY
     amadeus.api-secret=YOUR_API_SECRET
     amadeus.environment=test
6. Done — live flight search is active
```

### Rate Limit Strategy

| Concern | Solution |
|---|---|
| 2,000 calls/month free | Cache aggressively in Redis (5 min TTL) — identical searches hit cache |
| API down or slow | Automatic fallback to SkyWings DB flights |
| Rate limit exceeded | `ResponseException` caught → fallback to DB silently |
| Production upgrade | Change `amadeus.environment=production` (requires Amadeus approval) |

With Redis caching, the same origin-destination-date search only hits Amadeus **once every 5 minutes**, regardless of how many users search. A typical demo/portfolio scenario uses 10-50 calls/month — well within the 2,000 free limit.

---

## 9. Payment Simulation System

### Overview

This is a **simulated payment system** — no real money is involved. It mimics a real payment flow with card validation + OTP verification, but records transactions as `SIMULATED`.

### Flow

```
┌──────────────────────────────────────────────────────────────────┐
│                     PAYMENT SIMULATION FLOW                      │
│                                                                  │
│  1. User fills payment form:                                     │
│     - Card number (16 digits)                                    │
│     - Expiry date (MM/YY)                                        │
│     - CVV (3 digits)                                             │
│     - Payment method (Credit/Debit/UPI/Net Banking)              │
│     - Phone number (pre-filled from profile)                     │
│                                                                  │
│  2. POST /api/payments/initiate                                  │
│     Backend:                                                     │
│     a) Validates card number via Luhn algorithm                  │
│     b) Validates expiry date is in the future                    │
│     c) Validates CVV is 3 digits                                 │
│     d) Generates 6-digit OTP                                     │
│     e) Stores OTP in Redis with key "otp:{bookingId}"            │
│        TTL = 5 minutes                                           │
│     f) Sends OTP via BOTH email AND SMS simultaneously           │
│     g) Creates Payment record (status: PENDING)                  │
│     h) Returns: { transactionId, otpSentVia: ["EMAIL","SMS"] }   │
│                                                                  │
│  3. User receives OTP on phone (SMS) AND email                   │
│     User enters OTP on verification page                         │
│                                                                  │
│  4. POST /api/payments/verify-otp                                │
│     Backend:                                                     │
│     a) Retrieves OTP from Redis                                  │
│     b) Compares with user input                                  │
│     c) If match:                                                 │
│        - Payment status → COMPLETED                              │
│        - Booking status → CONFIRMED                              │
│        - Delete OTP from Redis                                   │
│        - Send confirmation email with e-ticket                   │
│     d) If no match:                                              │
│        - Increment attempt counter (max 3 attempts)              │
│        - Return error with remaining attempts                    │
│     e) If expired:                                               │
│        - Return error, prompt to resend                          │
│                                                                  │
│  5. POST /api/payments/resend-otp (optional)                     │
│     - Generate new OTP, send via requested channel               │
│     - Max 3 resends per payment                                  │
│                                                                  │
│  NOTE: No real payment processor is contacted.                   │
│        Transaction is recorded as type SIMULATED in the DB.      │
└──────────────────────────────────────────────────────────────────┘
```

### Luhn Algorithm (Card Validation)

```java
public class LuhnValidator {

    public static boolean isValid(String cardNumber) {
        // Remove spaces and dashes
        String sanitized = cardNumber.replaceAll("[\\s-]", "");

        // Must be 13-19 digits
        if (!sanitized.matches("\\d{13,19}")) return false;

        int sum = 0;
        boolean alternate = false;

        for (int i = sanitized.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(sanitized.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) digit -= 9;
            }
            sum += digit;
            alternate = !alternate;
        }

        return sum % 10 == 0;
    }

    public static String getLastFour(String cardNumber) {
        String sanitized = cardNumber.replaceAll("[\\s-]", "");
        return sanitized.substring(sanitized.length() - 4);
    }
}
```

**What this means for the user**: If they enter `4532015112830366` (a valid Luhn number), it passes. If they enter `1234567890123456` (invalid Luhn), it fails with "Invalid card number". This is the exact same check real payment processors use as a first step.

### Test Card Numbers (to document for users)

| Card Number | Type | Luhn Valid |
|---|---|---|
| 4532015112830366 | Visa | Yes |
| 5425233430109903 | Mastercard | Yes |
| 4111111111111111 | Visa (common test) | Yes |
| 1234567890123456 | Invalid | No |

---

## 10. OTP Verification — Email + SMS (Dual Channel)

### OtpService Implementation

```java
@Service
@Slf4j
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final TwilioSmsSender twilioSmsSender;
    private final EmailService emailService;

    private static final int OTP_LENGTH = 6;
    private static final long OTP_TTL_MINUTES = 5;
    private static final int MAX_ATTEMPTS = 3;
    private static final int MAX_RESENDS = 3;

    // Generate and send OTP via BOTH channels
    public OtpSendResult sendOtp(Long bookingId, String email, String phone) {
        String otp = generateOtp();
        String redisKey = "otp:" + bookingId;
        String attemptKey = "otp_attempts:" + bookingId;
        String resendKey = "otp_resends:" + bookingId;

        // Store OTP in Redis with 5-minute TTL
        redisTemplate.opsForValue().set(redisKey, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(attemptKey, "0", OTP_TTL_MINUTES, TimeUnit.MINUTES);

        // Send via both channels simultaneously
        List<String> sentVia = new ArrayList<>();
        boolean smsSent = false;
        boolean emailSent = false;

        try {
            twilioSmsSender.sendOtp(phone, otp);
            smsSent = true;
            sentVia.add("SMS");
        } catch (Exception e) {
            log.warn("SMS OTP failed for booking {}: {}", bookingId, e.getMessage());
        }

        try {
            emailService.sendOtpEmail(email, otp);
            emailSent = true;
            sentVia.add("EMAIL");
        } catch (Exception e) {
            log.warn("Email OTP failed for booking {}: {}", bookingId, e.getMessage());
        }

        if (!smsSent && !emailSent) {
            throw new OtpVerificationException("Failed to send OTP via any channel");
        }

        return new OtpSendResult(sentVia, OTP_TTL_MINUTES);
    }

    // Verify OTP
    public boolean verifyOtp(Long bookingId, String inputOtp) {
        String redisKey = "otp:" + bookingId;
        String attemptKey = "otp_attempts:" + bookingId;

        // Check attempts
        String attempts = redisTemplate.opsForValue().get(attemptKey);
        if (attempts != null && Integer.parseInt(attempts) >= MAX_ATTEMPTS) {
            redisTemplate.delete(redisKey);
            throw new OtpVerificationException("Maximum OTP attempts exceeded. Please request a new OTP.");
        }

        String storedOtp = redisTemplate.opsForValue().get(redisKey);
        if (storedOtp == null) {
            throw new OtpVerificationException("OTP expired. Please request a new OTP.");
        }

        if (storedOtp.equals(inputOtp)) {
            // Valid — clean up
            redisTemplate.delete(redisKey);
            redisTemplate.delete(attemptKey);
            return true;
        } else {
            // Invalid — increment attempts
            redisTemplate.opsForValue().increment(attemptKey);
            int remaining = MAX_ATTEMPTS - Integer.parseInt(attempts != null ? attempts : "0") - 1;
            throw new OtpVerificationException("Invalid OTP. " + remaining + " attempts remaining.");
        }
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);  // 6-digit number
        return String.valueOf(otp);
    }
}
```

### Twilio SMS Integration

**Dependencies (pom.xml):**
```xml
<dependency>
    <groupId>com.twilio.sdk</groupId>
    <artifactId>twilio</artifactId>
    <version>10.6.3</version>
</dependency>
```

**TwilioSmsSender:**
```java
@Component
public class TwilioSmsSender {

    @Value("${twilio.account-sid}")
    private String accountSid;

    @Value("${twilio.auth-token}")
    private String authToken;

    @Value("${twilio.phone-number}")
    private String fromNumber;

    @PostConstruct
    public void init() {
        Twilio.init(accountSid, authToken);
    }

    public void sendOtp(String toPhone, String otp) {
        Message.creator(
            new PhoneNumber(toPhone),   // To
            new PhoneNumber(fromNumber), // From (your Twilio number)
            "Your SkyWings Airways verification code is: " + otp +
            ". Valid for 5 minutes. Do not share this code."
        ).create();
    }
}
```

### Email OTP (via SendGrid / Spring Mail)

```java
@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public void sendOtpEmail(String toEmail, String otp) {
        Context context = new Context();
        context.setVariable("otp", otp);
        context.setVariable("validMinutes", 5);
        String htmlBody = templateEngine.process("otp-email", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject("SkyWings Airways - Payment Verification Code");
        helper.setText(htmlBody, true);
        helper.setFrom("noreply@skywings.com");
        mailSender.send(message);
    }
}
```

### OTP Email Template (`otp-email.html`)

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body style="font-family: Arial, sans-serif; max-width: 600px; margin: auto;">
    <div style="background: #1e3a5f; color: white; padding: 20px; text-align: center;">
        <h1>SkyWings Airways</h1>
    </div>
    <div style="padding: 30px; background: #f9f9f9;">
        <h2>Payment Verification</h2>
        <p>Your one-time verification code is:</p>
        <div style="background: #1e3a5f; color: white; font-size: 32px; letter-spacing: 8px;
                    padding: 15px 30px; text-align: center; margin: 20px 0; border-radius: 8px;">
            <span th:text="${otp}">123456</span>
        </div>
        <p>This code is valid for <strong th:text="${validMinutes}">5</strong> minutes.</p>
        <p style="color: #888; font-size: 12px;">
            If you did not initiate this payment, please ignore this email.
            No real payment will be charged — this is a simulated transaction.
        </p>
    </div>
</body>
</html>
```

### Dual-Channel UX Flow on Frontend

```
PaymentPage.jsx:
  ┌──────────────────────────────────────────────┐
  │  Payment                                      │
  │  ┌──────────────────────────────────────────┐ │
  │  │ Card Number: [4532 0151 1283 0366]       │ │
  │  │ Expiry:      [03/28]    CVV: [***]       │ │
  │  │ Method:      ● Credit  ○ Debit  ○ UPI   │ │
  │  │                                          │ │
  │  │ Total: Rs 12,750.00                      │ │
  │  │                                          │ │
  │  │        [ Proceed to Verify ]             │ │
  │  └──────────────────────────────────────────┘ │
  └──────────────────────────────────────────────┘
                      ↓
  ┌──────────────────────────────────────────────┐
  │  Verify Payment                               │
  │                                                │
  │  OTP sent to:                                  │
  │  📱 +91 ****7890 (SMS)                        │
  │  📧 r****@gmail.com (Email)                   │
  │                                                │
  │  Enter OTP: [ _ _ _ _ _ _ ]                    │
  │                                                │
  │  Resend OTP via:  [SMS]  [Email]               │
  │  Expires in: 4:32                              │
  │                                                │
  │        [ Verify & Confirm Booking ]            │
  └──────────────────────────────────────────────┘
```

---

## 11. E-Ticket & Boarding Pass Generation

### E-Ticket PDF Content

```
┌─────────────────────────────────────────────────────────┐
│  ✈ SKYWINGS AIRWAYS — Electronic Ticket                │
├─────────────────────────────────────────────────────────┤
│  Booking Reference: SKY-2026-A7B3                       │
│  Date of Issue: 01 Apr 2026                             │
│                                                         │
│  PASSENGER(S):                                          │
│  1. Rishabh Kumar    | Seat 3A | Economy | Passport: -- │
│  2. Aditya Singh     | Seat 3B | Economy | Passport: -- │
│                                                         │
│  FLIGHT DETAILS:                                        │
│  Flight: SW-101                                         │
│  Route:  Delhi (DEL) → Mumbai (BOM)                     │
│  Date:   15 Apr 2026                                    │
│  Depart: 08:00  |  Arrive: 10:15                        │
│  Type:   Domestic                                       │
│                                                         │
│  FARE BREAKDOWN:                                        │
│  Base Fare:     Rs 6,500.00 × 2.25 hrs × 2 pax         │
│  Subtotal:      Rs 29,250.00                            │
│  Tax (5%):      Rs 1,462.50                             │
│  Total:         Rs 30,712.50                            │
│                                                         │
│  Payment: ****0366 (Credit Card)                        │
│  Transaction: SKY-8A3F-C1D2                             │
│  Status: CONFIRMED                                      │
│                                                         │
│  [QR CODE]  ← Encodes: booking ID + flight + passengers │
│                                                         │
│  ⚠ This is a simulated booking. No real payment charged.│
└─────────────────────────────────────────────────────────┘
```

### Implementation

**TicketService.java:**
```java
@Service
public class TicketService {

    public byte[] generateETicketPdf(Booking booking) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        // Header
        Font headerFont = new Font(Font.HELVETICA, 20, Font.BOLD, new BaseColor(30, 58, 95));
        Paragraph header = new Paragraph("SKYWINGS AIRWAYS — Electronic Ticket", headerFont);
        header.setAlignment(Element.ALIGN_CENTER);
        document.add(header);
        document.add(new Paragraph("\n"));

        // Booking reference
        document.add(new Paragraph("Booking Reference: " + booking.getPayment().getTransactionId()));
        document.add(new Paragraph("Date of Issue: " +
            booking.getBookingDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))));

        // Passengers table
        PdfPTable passengerTable = new PdfPTable(4);
        passengerTable.setWidthPercentage(100);
        passengerTable.addCell("Passenger");
        passengerTable.addCell("Seat");
        passengerTable.addCell("Class");
        passengerTable.addCell("Passport");
        for (BookingPassenger bp : booking.getPassengers()) {
            passengerTable.addCell(bp.getPassengerName());
            passengerTable.addCell(bp.getSeat().getSeatNumber());
            passengerTable.addCell(booking.getSeatClass().name());
            passengerTable.addCell(bp.getPassportNumber() != null ? bp.getPassportNumber() : "—");
        }
        document.add(passengerTable);

        // Flight details, fare breakdown...
        // (similar structured paragraphs and tables)

        // QR Code
        byte[] qrImage = generateQrCode(booking);
        Image qr = Image.getInstance(qrImage);
        qr.scaleToFit(120, 120);
        document.add(qr);

        // Disclaimer
        Font disclaimer = new Font(Font.HELVETICA, 8, Font.ITALIC);
        document.add(new Paragraph(
            "This is a simulated booking. No real payment was charged.", disclaimer));

        document.close();
        return baos.toByteArray();
    }

    private byte[] generateQrCode(Booking booking) {
        String data = String.format("SKY|%s|%s|%s|%s",
            booking.getPayment().getTransactionId(),
            booking.getFlight().getFlightNumber(),
            booking.getBookingDate(),
            booking.getPassengers().size());

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(data, BarcodeFormat.QR_CODE, 300, 300);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(matrix);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }
}
```

### TicketController

```java
@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final BookingService bookingService;

    @GetMapping("/{bookingId}/eticket")
    public ResponseEntity<byte[]> downloadETicket(@PathVariable Long bookingId,
                                                   @AuthenticationPrincipal User user) {
        Booking booking = bookingService.getConfirmedBooking(bookingId, user);
        byte[] pdf = ticketService.generateETicketPdf(booking);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=SkyWings-ETicket-" + booking.getPayment().getTransactionId() + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdf);
    }
}
```

---

## 12. Email Notification System

### Email Types

| Event | Template | Recipients | Contains |
|---|---|---|---|
| Booking Confirmation | `booking-confirmation.html` | Passenger | Flight details, passengers, seats, fare breakdown, e-ticket PDF attachment |
| Payment OTP | `otp-email.html` | Passenger | 6-digit OTP, expiry time |
| Payment Receipt | `payment-receipt.html` | Passenger | Transaction ID, amount, card last 4, timestamp |
| Cancellation Notice | `cancellation-notice.html` | Passenger | Cancelled booking details, penalty amount, refund info |
| Flight Status Change | `flight-status.html` | All affected passengers | New status, rebooking options |

### NotificationService.java

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final TicketService ticketService;

    @Async  // Runs on a separate thread — doesn't block booking response
    public void sendBookingConfirmation(Booking booking) {
        try {
            Context context = new Context();
            context.setVariable("booking", booking);
            context.setVariable("flight", booking.getFlight());
            context.setVariable("passengers", booking.getPassengers());
            context.setVariable("payment", booking.getPayment());

            String html = templateEngine.process("booking-confirmation", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Booking Confirmed — " + booking.getFlight().getFlightNumber());
            helper.setText(html, true);
            helper.setFrom("noreply@skywings.com");

            // Attach e-ticket PDF
            byte[] eTicket = ticketService.generateETicketPdf(booking);
            helper.addAttachment("SkyWings-ETicket.pdf", new ByteArrayResource(eTicket));

            mailSender.send(message);
            log.info("Booking confirmation sent to {}", booking.getUser().getEmail());
        } catch (Exception e) {
            log.error("Failed to send booking confirmation for booking {}: {}",
                      booking.getId(), e.getMessage());
        }
    }
}
```

### SendGrid Configuration (application.yml)

```yaml
spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: ${SENDGRID_API_KEY}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

---

## 13. Frontend (React SPA)

### Page Routing

```jsx
// App.jsx
<Routes>
    {/* Public */}
    <Route path="/" element={<HomePage />} />
    <Route path="/login" element={<LoginPage />} />
    <Route path="/signup" element={<SignupPage />} />
    <Route path="/about" element={<AboutPage />} />
    <Route path="/flights" element={<FlightResultsPage />} />

    {/* Passenger (authenticated) */}
    <Route element={<ProtectedRoute />}>
        <Route path="/booking/:flightId" element={<BookingPage />} />
        <Route path="/payment/:bookingId" element={<PaymentPage />} />
        <Route path="/my-bookings" element={<MyBookingsPage />} />
        <Route path="/profile" element={<ProfilePage />} />
    </Route>

    {/* Admin */}
    <Route element={<ProtectedRoute requiredRole="ADMIN" />}>
        <Route path="/admin" element={<AdminDashboardPage />} />
        <Route path="/admin/flights" element={<ManageFlightsPage />} />
        <Route path="/admin/bookings" element={<ManageBookingsPage />} />
        <Route path="/admin/users" element={<ManageUsersPage />} />
    </Route>
</Routes>
```

### Axios Setup with JWT Interceptor

```javascript
// src/api/axios.js
import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
});

// Request interceptor — attach JWT
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

// Response interceptor — handle 401, refresh token
api.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401) {
            const refreshToken = localStorage.getItem('refreshToken');
            if (refreshToken) {
                try {
                    const { data } = await axios.post('/api/auth/refresh', { refreshToken });
                    localStorage.setItem('accessToken', data.token);
                    localStorage.setItem('refreshToken', data.refreshToken);
                    error.config.headers.Authorization = `Bearer ${data.token}`;
                    return api(error.config);  // Retry original request
                } catch {
                    localStorage.clear();
                    window.location.href = '/login';
                }
            }
        }
        return Promise.reject(error);
    }
);

export default api;
```

### Key UI Components

**FlightSearchForm.jsx** — the main hero component on the homepage:
- Origin airport dropdown (searchable, shows code + city)
- Destination airport dropdown
- Departure date picker (react-day-picker, min date = today)
- Seat class selector (Economy / Business)
- "Search Flights" button → navigates to `/flights?origin=DEL&dest=BOM&date=2026-04-15`

**SeatMap.jsx** — visual seat layout:
- Grid of seats per row (A B C — aisle — D E)
- Business class rows 1-2, Economy rows 3-8
- Color coding: green (available), gray (taken), blue (your selection)
- Click to select/deselect seats

**BookingStepper.jsx** — multi-step booking flow:
```
Step 1: Select Seats   →   Step 2: Passenger Details   →   Step 3: Review   →   Step 4: Payment
  [ ● ]──────────────────[ ○ ]──────────────────────[ ○ ]──────────────[ ○ ]
```

**OtpVerification.jsx** — OTP entry with countdown:
- 6 individual digit input boxes (auto-focus next on input)
- Countdown timer (5:00 → 0:00)
- "Resend via SMS" and "Resend via Email" buttons (appear after 30 seconds)
- 3-attempt limit with clear messaging

### Admin Dashboard

- **StatsCards**: Total flights, total bookings, active users, revenue (simulated)
- **FlightTable**: Sortable, paginated table with Add/Edit/Delete actions
- **BookingTable**: All bookings with status filters and search
- **UserTable**: All registered users with role badges

---

## 14. Testing Strategy

### Coverage Target: 80%+ for service layer, 70%+ overall

### Unit Tests (JUnit 5 + Mockito)

```java
// BookingServiceTest.java
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private FlightRepository flightRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private PricingService pricingService;
    @Mock private CacheManager cacheManager;

    @InjectMocks private BookingService bookingService;

    @Test
    void createBooking_withAvailableSeats_shouldSucceed() {
        // Given
        Flight flight = Flight.builder().id(1L).flightNumber("SW-101")
            .flightType("DOMESTIC").basePriceEconomy(new BigDecimal("6500")).build();
        List<Seat> seats = List.of(
            Seat.builder().id(1L).seatNumber("3A").isAvailable(true).build(),
            Seat.builder().id(2L).seatNumber("3B").isAvailable(true).build()
        );
        BookingRequest request = new BookingRequest(1L, SeatClass.ECONOMY,
            List.of(new PassengerDetail("Rishabh", 22, null)));

        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findAvailableSeatsByFlightAndClass(any(), any(), any()))
            .thenReturn(seats);
        when(pricingService.calculate(any(), any(), anyInt()))
            .thenReturn(new PricingResult(
                new BigDecimal("14625"), new BigDecimal("731.25"), new BigDecimal("15356.25")));
        when(bookingRepository.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(1L);
            return b;
        });

        // When
        BookingResponse result = bookingService.createBooking(request, testUser());

        // Then
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(result.getTotalPrice()).isEqualByComparingTo("15356.25");
        verify(seatRepository).saveAll(anyList());
    }

    @Test
    void createBooking_withInsufficientSeats_shouldThrow() {
        Flight flight = Flight.builder().id(1L).build();
        when(flightRepository.findById(1L)).thenReturn(Optional.of(flight));
        when(seatRepository.findAvailableSeatsByFlightAndClass(any(), any(), any()))
            .thenReturn(List.of());  // No seats

        BookingRequest request = new BookingRequest(1L, SeatClass.ECONOMY,
            List.of(new PassengerDetail("Rishabh", 22, null)));

        assertThatThrownBy(() -> bookingService.createBooking(request, testUser()))
            .isInstanceOf(InsufficientSeatsException.class);
    }
}
```

### Controller Tests (MockMvc)

```java
@WebMvcTest(FlightController.class)
class FlightControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private FlightService flightService;
    @MockBean private JwtService jwtService;

    @Test
    void searchFlights_shouldReturnResults() throws Exception {
        when(flightService.searchFlights("DEL", "BOM", LocalDate.of(2026, 4, 15), null))
            .thenReturn(List.of(/* mock responses */));

        mockMvc.perform(get("/api/flights/search")
                .param("origin", "DEL")
                .param("dest", "BOM")
                .param("date", "2026-04-15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }
}
```

### Integration Tests (Testcontainers)

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class BookingFlowIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired private TestRestTemplate restTemplate;

    @Test
    void fullBookingFlow_searchToConfirmation() {
        // 1. Sign up
        // 2. Log in → get JWT
        // 3. Search flights
        // 4. Create booking
        // 5. Initiate payment
        // 6. Verify OTP
        // 7. Verify booking is CONFIRMED
        // 8. Download e-ticket
    }
}
```

### Test Data Strategy

- Use `@Sql` annotations to load test data before each test
- Use `@Transactional` on tests for automatic rollback
- Testcontainers for PostgreSQL and Redis (real databases, not H2)

---

## 15. DevOps — Docker, CI/CD, Deployment

### docker-compose.yml (Local Development)

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: skywings-db
    environment:
      POSTGRES_DB: skywings
      POSTGRES_USER: skywings
      POSTGRES_PASSWORD: skywings_dev
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: skywings-redis
    ports:
      - "6379:6379"

  backend:
    build: ./backend
    container_name: skywings-api
    depends_on:
      - postgres
      - redis
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/skywings
      SPRING_DATASOURCE_USERNAME: skywings
      SPRING_DATASOURCE_PASSWORD: skywings_dev
      SPRING_DATA_REDIS_HOST: redis
      JWT_SECRET: dev-secret-key-change-in-production-must-be-256-bits
    volumes:
      - ./backend:/app
      - ~/.m2:/root/.m2  # Cache Maven dependencies

  frontend:
    build: ./frontend
    container_name: skywings-ui
    depends_on:
      - backend
    ports:
      - "5173:5173"
    environment:
      VITE_API_URL: http://localhost:8080/api

volumes:
  pgdata:
```

### Backend Dockerfile

```dockerfile
# Multi-stage build
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN ./mvnw dependency:resolve
COPY src src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### GitHub Actions CI/CD (`.github/workflows/ci-cd.yml`)

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env:
          POSTGRES_DB: skywings_test
          POSTGRES_USER: test
          POSTGRES_PASSWORD: test
        ports: ['5432:5432']
      redis:
        image: redis:7-alpine
        ports: ['6379:6379']

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'
      - name: Run Tests
        working-directory: ./backend
        run: ./mvnw verify
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/skywings_test
          SPRING_DATASOURCE_USERNAME: test
          SPRING_DATASOURCE_PASSWORD: test
          SPRING_DATA_REDIS_HOST: localhost

  build-and-deploy:
    needs: test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Build & Push Docker Image
        run: |
          echo ${{ secrets.GHCR_TOKEN }} | docker login ghcr.io -u ${{ github.actor }} --password-stdin
          docker build -t ghcr.io/${{ github.repository }}/backend:latest ./backend
          docker push ghcr.io/${{ github.repository }}/backend:latest
      - name: Deploy to Render
        run: curl -X POST ${{ secrets.RENDER_DEPLOY_HOOK }}
```

### Deployment Architecture

```
┌──────────────┐     ┌───────────────┐     ┌──────────────┐
│   Vercel     │     │    Render     │     │    Neon      │
│   (React)    │────→│ (Spring Boot) │────→│ (PostgreSQL) │
│   Frontend   │     │   Backend     │     │   Database   │
└──────────────┘     └───────┬───────┘     └──────────────┘
                             │
                      ┌──────┴──────┐
                      │   Upstash   │
                      │   (Redis)   │
                      └─────────────┘
```

- **Vercel**: Auto-deploys frontend on git push. Free. Custom domain support.
- **Render**: Deploys Docker container. Free tier (spins down after inactivity). Deploy hook triggered by GitHub Actions.
- **Neon**: Serverless PostgreSQL. Free tier with 0.5 GB. Connection pooling included.
- **Upstash**: Serverless Redis. Free tier with 10K commands/day. REST API or standard Redis protocol.

---

## 16. Design Patterns Used

| Pattern | Where | Implementation |
|---|---|---|
| **Repository** | Data access layer | `FlightRepository extends JpaRepository<Flight, Long>` — Spring Data auto-implements |
| **Service** | Business logic | `BookingService`, `PricingService`, `OtpService` — annotated with `@Service` |
| **DTO** | API boundary | Request/Response records — never expose JPA entities to API consumers |
| **Builder** | Object construction | Lombok `@Builder` on entities and DTOs — e.g., `Booking.builder().user(u).flight(f).build()` |
| **Strategy** | Pricing | `PricingService` uses different rates/taxes based on flight type + seat class. Extensible for loyalty discounts |
| **Observer / Event** | Notifications | Spring `@EventListener` or `@Async` — booking confirmed triggers email, PDF generation |
| **Factory Method** | Seat generation | When admin creates a flight, seats are auto-generated based on config (30 economy + 10 business) |
| **Adapter** | SMS / Email OTP | `OtpService` adapts Twilio (SMS) and Spring Mail (email) behind a unified `sendOtp()` interface |
| **Adapter** | Flight data sources | `AmadeusFlightService` adapts external Amadeus API behind the same response format; `FlightService` provides DB fallback — controller picks the best source |
| **Singleton** | Spring beans | All `@Service`, `@Component`, `@Repository` beans are singleton by default in Spring IoC |
| **Template Method** | Email templates | Thymeleaf templates with variable injection for each email type |
| **Filter Chain** | Security | `JwtAuthenticationFilter` in Spring Security filter chain — intercepts every request |
| **Facade** | Booking orchestration | `BookingService.createBooking()` coordinates flight lookup, seat assignment, pricing, persistence |

---

## 17. Implementation Phases & Order

### Phase 1 — Foundation (Days 1-3)

```
1.1  Project scaffolding
     - Initialize Spring Boot project (start.spring.io or mvnw archetype)
     - Configure pom.xml with all dependencies
     - Set up project structure (packages: entity, dto, repository, service, controller, config, exception)
     - Configure application.yml for dev profile

1.2  Docker setup
     - Create docker-compose.yml with PostgreSQL + Redis
     - Verify containers start: docker compose up -d
     - Test database connection

1.3  Entity layer
     - Create all entities: User, Flight, Seat, Booking, BookingPassenger, Payment
     - Create all enums: Role, SeatClass, BookingStatus, PaymentStatus, PaymentMethod, FlightStatus
     - Configure JPA relationships and constraints
     - Run app → verify Hibernate auto-creates tables

1.4  Seed data
     - Create data.sql with admin user + sample flights + auto-generated seats
```

### Phase 2 — Authentication (Days 4-5)

```
2.1  Security config
     - SecurityConfig.java: filter chain, CORS, public vs protected routes
     - BCrypt password encoder bean
     - JwtService: generate, validate, extract claims

2.2  Auth endpoints
     - POST /api/auth/signup — validate, hash password, save user, return JWT
     - POST /api/auth/login — validate credentials, return JWT
     - POST /api/auth/refresh — validate refresh token, issue new pair

2.3  JWT filter
     - JwtAuthenticationFilter: extract token from header, validate, set SecurityContext
     - Test with Postman/curl: signup → login → access protected route
```

### Phase 3 — Flight Management (Days 6-7)

```
3.1  Flight CRUD (admin)
     - FlightRepository with custom search query
     - FlightService: create (+ auto-generate seats), update, delete, search
     - AdminController: POST/PUT/DELETE flights
     - FlightController: GET search (public), GET flight details

3.2  Seat map
     - SeatRepository: find available by flight + class
     - FlightController: GET /api/flights/{id}/seats
     - Seat auto-generation logic when flight is created

3.3  Redis caching
     - RedisConfig: CacheManager bean
     - @Cacheable on searchFlights()
     - @CacheEvict when flights are modified or seats are booked

3.4  Amadeus API integration
     - Add amadeus-java dependency to pom.xml
     - AmadeusFlightService: real-time flight search via Amadeus Self-Service API
     - AmadeusFlightResponse DTO for live flight data
     - GET /api/flights/live-search endpoint in FlightController
     - Hybrid fallback: Amadeus first → SkyWings DB if unavailable
     - Redis caching for Amadeus results (5 min TTL)
     - Configure amadeus.api-key, api-secret, environment in application.yml
```

### Phase 4 — Booking System (Days 8-10)

```
4.1  Core booking
     - BookingService: createBooking with pessimistic locking on seats
     - PricingService: fare calculation (base × duration × seats + tax)
     - BookingController: POST /api/bookings, GET history, GET bill

4.2  Cancellation
     - BookingService: cancelBooking with penalty calculation
     - Release seats back to available
     - BookingController: DELETE /api/bookings/{id}

4.3  Booking expiry
     - @Scheduled task: cancel PENDING bookings older than 15 minutes
     - Release seats automatically

4.4  Exception handling
     - GlobalExceptionHandler with @RestControllerAdvice
     - Custom exceptions: ResourceNotFound, InsufficientSeats, etc.
     - Consistent ApiErrorResponse format
```

### Phase 5 — Payment + OTP (Days 11-13)

```
5.1  Payment simulation
     - PaymentService: initiatePayment (Luhn validation, create Payment record)
     - LuhnValidator utility
     - TransactionIdGenerator (SKY-xxxx-xxxx format)

5.2  OTP service
     - OtpService: generate, store in Redis, verify, track attempts
     - TwilioSmsSender: send SMS OTP
     - EmailService: send email OTP via Thymeleaf template

5.3  Payment flow
     - PaymentController: POST /initiate, POST /verify-otp, POST /resend-otp
     - On OTP verified: update payment + booking status, trigger confirmation email

5.4  Twilio setup
     - Create Twilio trial account
     - Get Account SID, Auth Token, phone number
     - Verify test phone numbers in Twilio console
     - Configure in application.yml
```

### Phase 6 — Tickets & Notifications (Days 14-15)

```
6.1  PDF generation
     - TicketService: e-ticket PDF with OpenPDF
     - QR code generation with ZXing
     - TicketController: GET download endpoints

6.2  Email notifications
     - NotificationService: booking confirmation, cancellation, payment receipt
     - Thymeleaf email templates (HTML)
     - @Async for non-blocking email sending
     - Attach e-ticket PDF to confirmation email

6.3  SendGrid setup
     - Create SendGrid account (free tier)
     - Get API key
     - Configure SMTP in application.yml
```

### Phase 7 — Frontend (Days 16-22)

```
7.1  React scaffolding (Day 16)
     - Vite + React + Tailwind + shadcn/ui setup
     - Axios instance with JWT interceptor
     - AuthContext + ProtectedRoute
     - Navbar + Footer + basic routing

7.2  Auth pages (Day 17)
     - LoginPage: email/password form, error handling, redirect
     - SignupPage: full registration form with validation
     - Profile page: view/edit profile, change password

7.3  Flight search (Days 18-19)
     - HomePage: hero section + FlightSearchForm
     - FlightResultsPage: list of FlightCards with sort/filter
     - SeatMap component: interactive visual seat selection

7.4  Booking flow (Days 19-20)
     - BookingPage: multi-step stepper
     - Step 1: seat selection (SeatMap)
     - Step 2: passenger details (PassengerForm for each)
     - Step 3: review summary (BookingSummary)
     - Transition to payment

7.5  Payment + OTP (Day 20-21)
     - PaymentPage: card form with Luhn validation on client
     - OTP verification: 6-digit input with countdown
     - Resend OTP buttons (SMS / Email)
     - Confirmation page with e-ticket download

7.6  My Bookings (Day 21)
     - MyBookingsPage: list of bookings with status badges
     - Cancel booking with penalty confirmation modal
     - Download e-ticket button

7.7  Admin pages (Day 22)
     - Dashboard with stats cards
     - Manage Flights: table + add/edit modal
     - Manage Bookings: table with filters
     - Manage Users: table
```

### Phase 8 — Testing (Days 23-24)

```
8.1  Unit tests
     - All service classes (BookingService, PricingService, OtpService, PaymentService, AuthService)
     - LuhnValidator, TransactionIdGenerator
     - Target: 80%+ coverage on service layer

8.2  Controller tests
     - MockMvc tests for all controllers
     - Test auth, validation, error responses

8.3  Integration tests
     - Testcontainers for PostgreSQL + Redis
     - Full booking flow: search → book → pay → confirm → download ticket
     - Full cancellation flow
     - Auth flow: signup → login → access → refresh

8.4  Frontend tests (optional, nice-to-have)
     - React Testing Library for key components
```

### Phase 9 — DevOps & Deploy (Days 25-26)

```
9.1  Docker
     - Backend Dockerfile (multi-stage build)
     - Frontend Dockerfile (nginx static serve)
     - Verify full stack runs via docker compose

9.2  CI/CD
     - GitHub Actions workflow: test → build → deploy
     - Environment secrets configuration

9.3  Cloud deployment
     - Neon: create PostgreSQL database
     - Upstash: create Redis instance
     - Render: deploy backend Docker container
     - Vercel: deploy frontend
     - Configure environment variables on each platform
     - Verify end-to-end flow on production URL

9.4  Final touches
     - API docs (Swagger UI) accessible at /swagger-ui.html
     - README.md with setup instructions, screenshots, architecture diagram
     - Update CORS for production frontend URL
```

---

## 18. Configuration & Environment Variables

### application.yml (dev)

```yaml
server:
  port: 8080

spring:
  application:
    name: skywings-airways

  datasource:
    url: jdbc:postgresql://localhost:5432/skywings
    username: skywings
    password: skywings_dev
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  data:
    redis:
      host: localhost
      port: 6379

  cache:
    type: redis
    redis:
      time-to-live: 300000  # 5 minutes in ms

  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: ${SENDGRID_API_KEY}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

  sql:
    init:
      mode: always  # Load data.sql on startup (dev only)

jwt:
  secret: ${JWT_SECRET:my-256-bit-secret-key-for-dev-only-change-in-prod-please}
  access-token-expiry: 1800000   # 30 minutes in ms
  refresh-token-expiry: 604800000  # 7 days in ms

twilio:
  account-sid: ${TWILIO_ACCOUNT_SID}
  auth-token: ${TWILIO_AUTH_TOKEN}
  phone-number: ${TWILIO_PHONE_NUMBER}

amadeus:
  api-key: ${AMADEUS_API_KEY:placeholder}
  api-secret: ${AMADEUS_API_SECRET:placeholder}
  environment: test  # "test" (free, cached data) or "production" (live, requires approval)

skywings:
  booking:
    expiry-minutes: 15
    max-seats-per-booking: 6
  otp:
    length: 6
    ttl-minutes: 5
    max-attempts: 3
    max-resends: 3
  payment:
    simulation-mode: true  # Always true — no real payments

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### .env.example

```bash
# Database (only needed if not using Docker Compose defaults)
DB_URL=jdbc:postgresql://localhost:5432/skywings
DB_USERNAME=skywings
DB_PASSWORD=skywings_dev

# JWT
JWT_SECRET=your-256-bit-secret-key-here-must-be-at-least-32-characters

# SendGrid (Email)
SENDGRID_API_KEY=SG.xxxxxxxxxxxxxxxxxxxx

# Twilio (SMS)
TWILIO_ACCOUNT_SID=ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
TWILIO_AUTH_TOKEN=your_auth_token
TWILIO_PHONE_NUMBER=+1234567890

# Amadeus (Real-time flight data)
AMADEUS_API_KEY=your_amadeus_api_key
AMADEUS_API_SECRET=your_amadeus_api_secret

# Frontend
VITE_API_URL=http://localhost:8080/api
```

---

## 19. Cost Summary

| Service | Free Tier | When You'd Pay |
|---|---|---|
| Java 21, Spring Boot, React, all libs | Forever free | Never |
| Docker Desktop | Free for personal | Never |
| GitHub (public repo) + Actions | 2,000 min/month CI | Private repo > limits |
| Neon (PostgreSQL) | 0.5 GB, always-on | > 0.5 GB data |
| Upstash (Redis) | 10K commands/day | > 10K commands/day |
| Render (backend) | 750 hrs/month | Remove cold start ($7/mo) |
| Vercel (frontend) | 100 GB bandwidth/month | Way beyond portfolio traffic |
| SendGrid (email) | 100 emails/day | > 100/day |
| Twilio (SMS) | ~1,900 OTPs with trial credit | After trial credit exhausted (~$0.008/SMS) |
| Amadeus API (flights) | 2,000 calls/month (no credit card) | > 2,000 calls/month (with Redis caching, typical usage is 10-50/month) |
| **Total monthly cost** | **$0** | **Only if you scale beyond portfolio usage** |

---

## Appendix A: Maven Dependencies (pom.xml)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.3.6</version>
</parent>

<properties>
    <java.version>21</java.version>
</properties>

<dependencies>
    <!-- Core -->
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-mail</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-redis</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-cache</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-thymeleaf</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-devtools</artifactId><scope>runtime</scope></dependency>

    <!-- Database -->
    <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>

    <!-- JWT -->
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.12.6</version></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.12.6</version><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.12.6</version><scope>runtime</scope></dependency>

    <!-- Twilio SMS -->
    <dependency><groupId>com.twilio.sdk</groupId><artifactId>twilio</artifactId><version>10.6.3</version></dependency>

    <!-- Amadeus Flight Data -->
    <dependency><groupId>com.amadeus</groupId><artifactId>amadeus-java</artifactId><version>9.1.0</version></dependency>

    <!-- PDF Generation -->
    <dependency><groupId>com.github.librepdf</groupId><artifactId>openpdf</artifactId><version>2.0.3</version></dependency>

    <!-- QR Code -->
    <dependency><groupId>com.google.zxing</groupId><artifactId>core</artifactId><version>3.5.3</version></dependency>
    <dependency><groupId>com.google.zxing</groupId><artifactId>javase</artifactId><version>3.5.3</version></dependency>

    <!-- API Docs -->
    <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>2.6.0</version></dependency>

    <!-- Lombok -->
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>

    <!-- Testing -->
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.testcontainers</groupId><artifactId>postgresql</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.testcontainers</groupId><artifactId>junit-jupiter</artifactId><scope>test</scope></dependency>
</dependencies>
```

---

## Appendix B: Comparison — Old vs New

| Aspect | Old (3rd Sem) | New (Rewrite) |
|---|---|---|
| UI | Java Swing (desktop) | React SPA (web browser) |
| Backend | Plain Java classes | Spring Boot 3.3 REST API |
| Database | In-memory ArrayList | PostgreSQL + Redis cache |
| Auth | Plaintext password comparison | BCrypt + JWT + Spring Security |
| Flight Data | Hardcoded in-memory | Real-time from Amadeus API (with DB fallback) |
| Payments | None | Simulated with Luhn + dual-channel OTP |
| Notifications | None | Email (SendGrid) + SMS (Twilio) |
| Tickets | None | PDF e-ticket with QR code |
| Testing | None | JUnit 5 + Mockito + Testcontainers |
| Deployment | Local only (Eclipse) | Docker + CI/CD + Cloud (Render/Vercel) |
| API Docs | None | Swagger UI auto-generated |
| Error Handling | JOptionPane dialogs | Global exception handler + structured errors |
| Architecture | Monolithic god class | Layered (Controller → Service → Repository) |
| Concurrency | Not thread-safe | Pessimistic locking + optimistic locking |
| Code Quality | 1000+ lines commented code | Clean, documented, tested |

---

*This document is the single source of truth for the SkyWings Airways rewrite. All implementation decisions, technical choices, and architecture patterns are defined here. Refer to this before making any architectural changes.*
