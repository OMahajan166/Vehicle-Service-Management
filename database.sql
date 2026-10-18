-- ============================================================
--  Vehicle Service Management System — Database Script
--  MySQL 8.x
--
--  NOTE: You normally DON'T need to run this file — the app
--  (Hibernate, ddl-auto=update) creates/updates tables itself.
--  This script is provided for manual setup / viva reference.
--  Just create the empty database and start the app:
--
--      CREATE DATABASE vehicle_service_db;
--
--  Run this file only if you want the schema created manually.
-- ============================================================

CREATE DATABASE IF NOT EXISTS vehicle_service_db;
USE vehicle_service_db;

-- ---------------- Users (ADMIN / CUSTOMER / MECHANIC logins) ----------------
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,   -- BCrypt-encoded
    phone      VARCHAR(255) NOT NULL,
    role       VARCHAR(255) NOT NULL,   -- ADMIN, CUSTOMER, MECHANIC
    created_at DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

-- ---------------- Vehicles ----------------
CREATE TABLE IF NOT EXISTS vehicles (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    vehicle_number VARCHAR(255) NOT NULL,
    model          VARCHAR(255) NOT NULL,
    company        VARCHAR(255) NOT NULL,
    fuel_type      VARCHAR(255) NOT NULL,  -- Petrol, Diesel, CNG, Electric
    customer_id    BIGINT       NOT NULL,  -- -> users.id (CUSTOMER)
    created_at     DATETIME(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_vehicles_number (vehicle_number)
) ENGINE=InnoDB;

-- ---------------- Mechanics ----------------
CREATE TABLE IF NOT EXISTS mechanics (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    name           VARCHAR(255) NOT NULL,
    specialization VARCHAR(255) NOT NULL,
    phone          VARCHAR(255) NOT NULL,
    email          VARCHAR(255) NOT NULL,
    experience     VARCHAR(255) NOT NULL,
    user_id        BIGINT,                 -- -> users.id (MECHANIC login), NULL = no login yet
    created_at     DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ---------------- Service bookings / job cards ----------------
CREATE TABLE IF NOT EXISTS services (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    customer_id  BIGINT       NOT NULL,  -- -> users.id
    vehicle_id   BIGINT       NOT NULL,  -- -> vehicles.id
    service_type VARCHAR(255) NOT NULL,
    description  VARCHAR(255) NOT NULL,
    service_date DATE         NOT NULL,
    status       VARCHAR(255) NOT NULL,  -- Pending, Accepted, In Progress, Completed, Cancelled
    mechanic_id  BIGINT,                 -- -> mechanics.id
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ---------------- Bills / invoices ----------------
CREATE TABLE IF NOT EXISTS bills (
    id             BIGINT  NOT NULL AUTO_INCREMENT,
    service_id     BIGINT  NOT NULL,     -- -> services.id
    customer_id    BIGINT  NOT NULL,     -- -> users.id
    labor_cost     DOUBLE  NOT NULL,
    spare_parts    DOUBLE  NOT NULL,
    tax            DOUBLE  NOT NULL,
    total_amount   DOUBLE  NOT NULL,     -- labor + spare parts + tax
    status         VARCHAR(255) NOT NULL, -- Pending, Paid
    payment_method VARCHAR(255),          -- UPI, Card, Net Banking, Cash
    paid_at        DATETIME(6),
    created_at     DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ---------------- Contact messages (website form) ----------------
CREATE TABLE IF NOT EXISTS contacts (
    id         BIGINT        NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255)  NOT NULL,
    email      VARCHAR(255)  NOT NULL,
    phone      VARCHAR(255)  NOT NULL,
    message    VARCHAR(1000) NOT NULL,
    created_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ============================================================
--  Default admin is created automatically by the app on first
--  run (DataInitializer):   admin@example.com / admin123
--  Change the password after first login in a real deployment.
-- ============================================================
