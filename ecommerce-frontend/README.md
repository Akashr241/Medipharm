# MediPharm — AI-Powered Pharmacy Platform

An AI-powered pharmacy e-commerce application built with **Spring Boot, PostgreSQL and React**. MediPharm evolved from my production E-Commerce project (JWT auth, cart, checkout, Razorpay payments) and adds a prescription pipeline: upload a prescription, extract the text with OCR, and match it to real medicines.

> **Status:** Actively in development (since Dec 2025). The core commerce flow is working; the prescription AI pipeline and deployment are in progress.

🔗 **Live Demo:** [ADD-FRONTEND-LINK-HERE](https://medipharm-eosin.vercel.app/)
🔗 **Backend API:** https://medipharm-backend-91t4.onrender.com



## Features

- **Prescription upload with OCR** — extracts text from prescription images using Tesseract
- **AI medicine name normalization** — cleans up raw OCR text into proper medicine names
- **Fuzzy medicine search** — PostgreSQL `pg_trgm` trigram matching over 250k+ Indian medicine records, so typos and OCR misreads still find the right medicine
- **AI-generated image detection** — Java-native heuristic (EXIF metadata + pixel-level analysis) that screens uploaded prescriptions before OCR, with no external API
- **Medicine catalog and cart** — browse, search and add medicines to the cart
- **Payments** — Razorpay payment flow (simulation on the frontend)
- **Reminders** — medicine reminder feature on the frontend
- **Secure backend** — JWT authentication and role-based authorization with Spring Security

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java, Spring Boot, Spring Security, Spring Data JPA, Hibernate |
| Database | PostgreSQL (`pg_trgm` extension) |
| Frontend | React.js |
| OCR | Tesseract |
| Payments | Razorpay |
| Auth | JWT, role-based authorization |
| Tools | Maven, Postman, Swagger, Git |
| Deployment | Vercel (frontend), Render / Railway (backend) |

---

## How the Prescription Pipeline Works

```
Upload image → AI-image check (EXIF + pixel analysis) → Tesseract OCR
→ Name normalization → pg_trgm fuzzy match against medicine dataset → Add to cart
```

---

## Dataset

Medicine data comes from the open-source [Indian Medicine Dataset](https://github.com/junioralive/Indian-Medicine-Dataset) (CSV), self-hosted in PostgreSQL.

---

## Getting Started

### Prerequisites

- Java 17+ (or the version your project uses)
- Maven
- PostgreSQL (with the `pg_trgm` extension enabled)
- Node.js and npm
- Tesseract OCR installed locally


### 2. Set up the database

```sql
CREATE DATABASE medipharm;
\c medipharm
CREATE EXTENSION IF NOT EXISTS pg_trgm;
```

Then import the medicine dataset CSV into your medicines table.

### 3. Run the backend

Set your database and secret values in `application.properties` (or environment variables), then:

```bash
cd backend
mvn spring-boot:run
```

### 4. Run the frontend

```bash
cd frontend
npm install
npm run dev
```

The frontend runs on `http://localhost:5173` by default.

---

## Roadmap

- [ ] Containerize the Spring Boot backend with Docker
- [ ] Complete backend-to-frontend integration for the prescription flow
- [ ] Deploy backend and frontend
- [ ] Add integration tests

---

## Author

**Akash R** — Java Full Stack Developer

- 📧 [akashr.offical7@gmail.com](mailto:akashr.offical7@gmail.com)
- 💼 [LinkedIn](https://www.linkedin.com/in/akashr5)
- 🐙 [GitHub](https://github.com/Akashr241)
- 🌐 [Portfolio](https://portfolio-main-lime-mu.vercel.app)