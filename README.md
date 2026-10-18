# 🚗 Vehicle Service Management System (AutoCare Services)

A full-stack web application to manage a vehicle service centre/garage —
customer vehicle management, service bookings (job cards), mechanic
assignment & tracking, billing with online (simulated) payment, printable
invoices, and admin reports.

Built as a college project with **Spring Boot 3, Thymeleaf, Spring Security,
Spring Data JPA and MySQL**.

---

## 👥 User Roles & Features

### 🧑 Customer
- Register / Login (BCrypt-secured)
- Add, **edit & delete** vehicles (duplicate vehicle-number protection;
  vehicles with service history can't be deleted)
- Book a service (past dates are rejected)
- Track service status live (Pending → Accepted → In Progress → Completed)
- View bills, **pay online** (UPI / Card / Net Banking / Cash — simulated),
  and **print/download invoice** (PDF-ready)

### 👨‍💼 Admin
- Dashboard with business statistics
- Manage customers (**search** by name / email / phone)
- Manage mechanics — **add / edit / delete** (creates a mechanic login;
  mechanics with active jobs can't be deleted)
- Manage services — assign mechanic, update status, **search & filter**
- Generate bills (customer auto-matched to the service; duplicate bills blocked)
- **Reports**: total / collected / pending revenue, **revenue-by-month chart**,
  **services-by-status chart**, and **CSV export** (Bills / Services / Customers)
- View website contact messages

### 🔧 Mechanic
- Separate login (created by admin) with a dedicated dashboard
- Sees only jobs assigned to them; updates job status
  (Accepted → In Progress → Completed), visible to the customer instantly

### 🌐 Public
- Home, About, Services, Contact form (validated)

---

## 🛠 Tech Stack
| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.1.5, Spring MVC |
| Security | Spring Security (role-based), BCrypt |
| View | Thymeleaf, HTML/CSS |
| Database | MySQL 8, Spring Data JPA / Hibernate |
| Build | Maven |

---

## ▶️ How to Run

### Prerequisites
- JDK 21 (or 17+)
- Maven
- MySQL 8 running locally

### 1. Create the database
```sql
CREATE DATABASE vehicle_service_db;
```
*(Tables are created automatically by Hibernate on first run.
`database.sql` contains the full schema for manual setup/reference.)*

### 2. Configure the database connection
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/vehicle_service_db
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

### 3. Run the application
```bash
mvn spring-boot:run
```
or build & run the jar:
```bash
mvn clean package -DskipTests
java -jar target/vehicle-service-management-1.0.0.jar
```

### 4. Open in browser
http://localhost:8080

**Default admin login** (auto-created on first run):
```
Email:    admin@example.com
Password: admin123
```
Customers can register from the Register page.
Mechanics are added by the admin (Admin → Mechanics), who sets their
login password; mechanics then log in with their email + that password.

---

## 🗂 Project Structure
```
src/main/java/com/project/
├── VehicleServiceApplication.java   # main class
├── config/        # Spring Security, user details, default-admin seed
├── controller/    # Auth, Home, Contact, Customer, Admin, Mechanic
├── service/       # business logic
├── repository/    # Spring Data JPA repositories
├── model/         # entities: User, Vehicle, Service, Mechanic, Bill, Contact
└── exception/     # global exception handler

src/main/resources/
├── application.properties
├── templates/     # Thymeleaf pages (public / customer / admin / mechanic)
└── static/css/    # stylesheet
database.sql       # full MySQL schema (reference/manual setup)
PROJECT_REVIEW.md  # development & review notes
```

---

## 💳 Note on Payments
Payments are **simulated** for demo purposes — no real money moves and no
payment gateway is integrated. A real gateway (e.g. Razorpay/Stripe in test
mode) can be integrated at the `payBill` step in `BillService`.

## 🔒 Security Notes
- Passwords are stored BCrypt-encoded.
- Role-based access: `/admin/**` (ADMIN), `/customer/**` (CUSTOMER),
  `/mechanic/**` (MECHANIC); ownership checks prevent users from
  viewing/paying other customers' bills or invoices.
- Do not commit a real database password to a public repository.
