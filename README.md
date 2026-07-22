# 🌿 Flara API — Spring Boot Backend

**Frontend repo:** [flara](https://github.com/Lunaris47/flara)  
**Live API:** [flara-api-production.up.railway.app](https://flara-api-production.up.railway.app)

REST API backend for [Flara](https://flara-five.vercel.app), a holistic IBD health tracking app. Built with Java 21 and Spring Boot 4.1, deployed on Railway with a PostgreSQL database on Neon.

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 4.1 |
| Security | Spring Security + JWT (jjwt 0.12.6) |
| ORM | Spring Data JPA + Hibernate |
| Database | PostgreSQL (Neon) |
| PDF Generation | iText 5.5.13 |
| Deployment | Railway |

---

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive JWT token |

### Daily Logs
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/logs/physical` | Save a physical check-in |
| GET | `/api/logs/physical` | Get all physical logs |
| GET | `/api/logs/physical/date/{date}` | Get physical log by date |
| PUT | `/api/logs/physical/{id}` | Update a physical log |
| DELETE | `/api/logs/physical/{id}` | Delete a physical log |
| POST | `/api/logs/mental` | Save a mental check-in |
| GET | `/api/logs/mental` | Get all mental logs |
| GET | `/api/logs/mental/date/{date}` | Get mental log by date |
| PUT | `/api/logs/mental/{id}` | Update a mental log |
| DELETE | `/api/logs/mental/{id}` | Delete a mental log |

### Meals
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/meals` | Log a meal |
| GET | `/api/meals` | Get all meal logs |
| GET | `/api/meals/date/{date}` | Get meals by date |
| PUT | `/api/meals/{id}` | Update a meal log |
| DELETE | `/api/meals/{id}` | Delete a meal log |

### Medications
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/medications` | Log a medication |
| GET | `/api/medications` | Get all medication logs |
| DELETE | `/api/medications/{id}` | Delete a medication log |

### Flares
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/flares` | Record a flare |
| GET | `/api/flares` | Get all flares |
| PUT | `/api/flares/{id}` | Update a flare (resolve, add symptoms) |
| DELETE | `/api/flares/{id}` | Delete a flare |

### Reports
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/report/{days}` | Generate PDF health report (30/60/90 days) |

### User
| Method | Endpoint | Description |
|--------|----------|-------------|
| DELETE | `/api/user/delete` | Delete account and all associated data |

All endpoints except `/api/auth/**` require a valid JWT Bearer token.

---

## Data Model

```
users
├── DailyPhysicalLog (pain score, bowel tracking, symptoms, energy)
├── DailyMentalLog (stress score, mood, sleep, anxiety, meditation)
├── MealLog (description, safety rating, trigger tags, flare correlation)
├── MedicationLog (medication name, type, taken/missed, biologic date)
└── Flare (start/end datetime, severity, physical/mental context, triggers)
```

---

## Getting Started

### Prerequisites
- Java 21
- Maven
- PostgreSQL database (or a free Neon account)

### Environment Variables

Create an `application.properties` or set these environment variables:

```properties
DB_URL=jdbc:postgresql://<host>/<database>?sslmode=require
DB_USERNAME=<username>
DB_PASSWORD=<password>
JWT_SECRET=<64-character hex string>
JWT_EXPIRATION=86400000
```

To generate a JWT secret:
```bash
openssl rand -hex 32
```

### Running Locally

```bash
# Clone the repo
git clone https://github.com/Lunaris47/flara-api.git
cd flara-api

# Run with Maven
./mvnw spring-boot:run
```

The API starts at `http://localhost:8080`.

---

## Project Structure

```
src/main/java/com/flara/
├── controller/
│   ├── AuthController.java         # Register and login
│   ├── DailyLogController.java     # Physical and mental log endpoints
│   ├── FlareController.java        # Flare CRUD
│   ├── MealLogController.java      # Meal log CRUD
│   ├── MedicationLogController.java # Medication log CRUD
│   ├── PdfReportController.java    # PDF report generation
│   └── UserController.java         # Delete account
├── entity/
│   ├── DailyMentalLog.java
│   ├── DailyPhysicalLog.java
│   ├── Flare.java                  # Uses LocalDateTime for precise time tracking
│   ├── MealLog.java
│   ├── MedicationLog.java
│   └── User.java
├── repository/
│   ├── DailyMentalLogRepository.java
│   ├── DailyPhysicalLogRepository.java
│   ├── FlareRepository.java
│   ├── MealLogRepository.java
│   ├── MedicationLogRepository.java
│   └── UserRepository.java
├── security/
│   ├── FlaraUserDetailsService.java # Loads user from DB for Spring Security
│   ├── JwtFilter.java               # Validates JWT on every request
│   ├── JwtUtil.java                 # Token generation and validation
│   └── SecurityConfig.java          # CORS, filter chain, auth provider
├── service/
│   ├── AuthService.java             # Register and login logic
│   ├── DailyLogService.java         # Physical and mental log business logic
│   ├── FlareService.java
│   ├── MealLogService.java
│   ├── MedicationLogService.java
│   └── PdfReportService.java        # iText PDF generation
└── dto/
    ├── AuthResponse.java
    ├── LoginRequest.java
    └── RegisterRequest.java
```

---

## Related

- **Frontend:** [github.com/Lunaris47/flara](https://github.com/Lunaris47/flara)
- **Live app:** [flara-five.vercel.app](https://flara-five.vercel.app)
