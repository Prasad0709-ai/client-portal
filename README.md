# Apex Agency Client Portal

A modern, full-stack Java web application built with **Spring Boot 3.3**, **Spring Security 6**, **Hibernate / Spring Data JPA**, **MySQL / H2**, and **Thymeleaf**. Designed specifically for agencies and freelancers to manage client engagements, project milestones, deliverable approvals, invoicing, file repositories, and team communications.

---

## 🚀 Key Features

- **Project Milestones & Progress Tracking**
  - Interactive timeline view with real-time progress percentage recalculation.
  - Interactive check-off milestones with live AJAX updates without page reloads.
  - Multi-tab project workspace (`Milestones`, `Deliverables`, `Invoices`, `Files`, `Discussion`).

- **Deliverables & Client Review Workflow**
  - Agency team submits deliverables with versioning and staging URLs.
  - Clients can officially **Approve** or **Request Changes** with feedback notes.
  - Real-time review status badges (`Pending Review`, `Approved`, `Changes Requested`).

- **Invoicing & Billing Center**
  - Executive financial summary dashboard (Settled revenue, Outstanding balance, Overdue alerts).
  - Clean, printable invoice statement (`window.print()` / PDF export).
  - Integrated payment simulator modal (Card, ACH Bank Debit, Wire transfer).

- **Categorized Document & Asset Vault**
  - Filterable file repository by category (`Contract & SOW`, `Design & Specs`, `Deliverable Asset`, `Brand Guidelines`, etc.).
  - Secure upload handling with direct streaming file downloads.

- **Real-Time Project Discussions**
  - Dedicated communication channel per client engagement.
  - Client vs. Agency message bubbles with timestamps and read indicators.
  - Background polling REST endpoint for seamless updates.

- **Role-Based Access Control (RBAC)**
  - `ROLE_ADMIN` (Agency Lead / PM): Full access to create/edit projects, issue invoices, publish deliverables, and manage milestones.
  - `ROLE_CLIENT` (Client Contact): Access scoped strictly to their company's projects, deliverables, invoices, and files.

---

## 🛠️ Technology Stack & Architecture

- **Backend**: Java 21, Spring Boot 3.3.4
- **Security**: Spring Security 6 with BCrypt password hashing, session management, CSRF protection, and role-based method security (`@PreAuthorize`).
- **Persistence**: Hibernate 6 / Spring Data JPA with `JOIN FETCH` query optimizations.
- **Database**:
  - Embedded H2 database configured by default for zero-friction local execution.
  - Full MySQL dialect and driver support configured in `application-mysql.properties`.
- **Frontend / View Layer**: Thymeleaf 3 with Spring Security extras (`sec:authorize`).
- **Styling**: Custom modern design system (`portal.css`) featuring glassmorphism, responsive sidebar, Outfit & Plus Jakarta Sans typography, and subtle micro-interactions.
- **Interactive Layer**: Vanilla JavaScript (`portal.js`) with asynchronous REST API integrations.

---

## 🏃 Getting Started

### Prerequisites
- **Java 21** or later (`java -version`)
- **Maven 3.9+** (`mvn -v`)

### Running the Application

1. Open your terminal in the project directory:
   ```bash
   cd C:\Users\pujar\.gemini\antigravity-ide\scratch\client-portal
   ```

2. Run with Maven:
   ```bash
   mvn spring-boot:run
   ```

3. Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

### Running with MySQL

By default, the application runs on H2 in-memory mode for instant execution. To run against your local MySQL service:

1. Create database:
   ```sql
   CREATE DATABASE client_portal;
   ```
2. Run with the MySQL profile:
   ```bash
   mvn spring-boot:run -Dspring-boot.run.profiles=mysql
   ```
   *(Update username/password in `src/main/resources/application-mysql.properties` if needed)*

---

## 🔑 Demo Login Accounts

The application automatically seeds realistic data on first startup:

| Role | Username | Password | Display Name & Organization |
| :--- | :--- | :--- | :--- |
| **Agency Admin / PM** | `admin` | `admin123` | Alex Morgan &bull; Apex Studio Agency |
| **Client 1** | `acme_client` | `client123` | Sarah Connor &bull; Acme Global Technologies |
| **Client 2** | `nexus_client` | `client123` | David Zhang &bull; Nexus Intelligence Inc |

> **Tip**: The login screen features **1-click quick-fill buttons** to effortlessly test between Agency Admin and Client personas.

---

## 📂 Project Structure

```
client-portal/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/agency/clientportal/
│   │   │   ├── ClientPortalApplication.java
│   │   │   ├── config/             # SecurityConfig, DataInitializer
│   │   │   ├── controller/         # Auth, Dashboard, Project, Deliverable, Invoice, File, Message, RestApi
│   │   │   ├── dto/                # Form validation objects (@Valid)
│   │   │   ├── entity/             # User, Role, Project, Milestone, Deliverable, Invoice, FileDocument, Message
│   │   │   ├── repository/         # Spring Data JPA repositories with custom queries
│   │   │   └── service/            # Transactional business logic & Security user details
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-mysql.properties
│   │       ├── static/
│   │       │   ├── css/portal.css  # Modern CSS design system
│   │       │   └── js/portal.js    # Interactive modal, tab, and AJAX scripts
│   │       └── templates/          # Thymeleaf templates (layout, auth, dashboard, projects, etc.)
│   └── test/                       # Spring Boot integration tests
```
