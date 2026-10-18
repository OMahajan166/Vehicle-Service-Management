# Vehicle Service Management System — Project Review
_Date: 18 Oct 2026 · Stack: Spring Boot 3.1.5 · Java 21 · MySQL · Thymeleaf · Spring Security_

## ✅ What is already built
- **Auth:** Register / Login with Spring Security + BCrypt, role-based access (ADMIN / CUSTOMER). Default admin is auto-created on first run: `admin@example.com` / `admin123` (change this!).
- **Customer:** Dashboard with stats, Add/List vehicles, Book service, My Services, Track service (with ownership check), My Bills, View bill.
- **Admin:** Dashboard with stats (customers, mechanics, pending/completed, revenue), Manage customers, Add/List mechanics, Manage services (assign mechanic), Generate bill, Bills list, Reports page, View contact messages.
- **Public pages:** Home, About, Services, Contact form (with validation).
- **Validation & safety:** Bean validation on forms, duplicate vehicle-number check, duplicate email check, customers can only see their own services/bills.

## 🔧 Fixed in this review (18 Oct 2026)
1. **Broken Login link** on About page (`/login` → `/auth/login`).
2. **Broken Logout links** on Admin Dashboard & Customers pages (`/auth/logout` → `/logout`) — these previously led to a 404.
3. **Missing "Update Service Status" UI** — the backend endpoint `POST /admin/update-service-status/{id}` existed but no page used it, so a service could never move Pending → In Progress → Completed. Added a status dropdown (Pending / Accepted / In Progress / Completed / Cancelled) on Admin → Manage Services.
4. **Bill generation could bill the wrong customer** — admin had to hand-type a Customer ID. Now the customer is derived automatically from the selected service, and generating a **second bill for the same service is blocked** with an error message.

Verified: `mvn compile` passes on Java 21 after all fixes.

## 💳 Added 18 Oct 2026 — Pay Bill + Print Invoice
- **Pay Bill:** On My Bills → Pay Now (or the bill page), the customer picks a payment
  method (UPI / Card / Net Banking / Cash), confirms, and the bill becomes `Paid`.
  New DB columns on `bills`: `payment_method`, `paid_at` (auto-added by Hibernate).
  Double payment is blocked; only the bill's owner can pay/print (ownership checked).
  _Payment is simulated — no real gateway (Razorpay/Stripe test mode could be added)._
- **Print Invoice:** After payment, "Print Invoice" opens a printable invoice
  (`/customer/bill/{id}/print`) with customer, vehicle & service details, charge
  breakdown, PAID stamp, and auto-opens the print dialog (can Save as PDF).
- **End-to-end tested live** (register → add vehicle → book service → admin completes
  service → admin generates bill ₹2,006 → customer pays via UPI → invoice shows
  PAID/customer/vehicle/total; duplicate bill & double-pay correctly blocked).

## ✏️ Added 18 Oct 2026 — Edit / Delete for Vehicles & Mechanics
- **Vehicles (customer):** Edit (pre-filled form, duplicate vehicle-number check that
  correctly allows keeping your own number) and Delete — both owner-only.
  A vehicle with service bookings **cannot be deleted** (protects service history).
- **Mechanics (admin):** Edit (pre-filled form) and Delete. A mechanic with
  **active (unfinished) jobs cannot be deleted** — admin is told how many active
  jobs to reassign/complete first. Completed-job history is preserved.
- All actions have confirm dialogs + success/error messages.
- **Live-tested:** edit vehicle (incl. duplicate-number rejection), delete clean
  vehicle, blocked delete with history, other-customer access blocked, edit
  mechanic, delete job-free mechanic, blocked delete with 1 active job,
  delete allowed after job Completed — all passed.

## 📅🔧 Added 18 Oct 2026 — Booking Date Validation + Mechanic Login & Dashboard
- **Date validation:** Past service dates are now rejected ("Service date cannot be
  in the past" — `@FutureOrPresent` on `Service.serviceDate` + date-picker minimum).
  Today and future dates work.
- **Mechanic accounts:** Adding a mechanic now also creates a login (email +
  password set by admin, role `MECHANIC`, new `mechanics.user_id` link column).
  Mechanics log in on the normal Login page and land on **`/mechanic/dashboard`**:
  stats (total/active/in-progress/completed jobs) + their assigned jobs, and they
  can move a job Accepted → In Progress → Completed (customers see it live on
  Track Service). Mechanics can only touch **their own** jobs; invalid statuses
  are rejected; `/admin/**` and `/customer/**` return 403 for mechanics (and
  vice-versa).
- **Admin extras:** Edit Mechanic can reset the mechanic's password (and keeps
  name/email/phone in sync with the login account); deleting a mechanic also
  deletes their login. Mechanics added *before* this feature get a login by
  setting a password on their Edit page.
- **Live-tested:** past-date rejected (no booking saved), today/future accepted,
  mechanic creation + duplicate-email block, login redirect, job status flow
  visible to customer, other-mechanic tampering blocked, 403 role isolation,
  invalid status rejected, password reset (old fails / new works), delete
  removes login, legacy mechanic upgrade — all passed.

## 🔍 Added 18 Oct 2026 — Search & Filter on Admin Lists
- **Customers:** search by name, email or phone (case-insensitive).
- **Services:** keyword (type / description / ID / customer / vehicle) + status
  dropdown (Pending / Accepted / In Progress / Completed / Cancelled), combinable.
- **Bills:** keyword (Bill / Service / Customer ID) + status (Pending / Paid).
- **Mechanics:** search by name, specialization, email or phone.
- Every list shows a result count, a Clear button, and a friendly
  "No … match your search/filter" empty state. Filtering is done server-side;
  the Update-Status / Assign-Mechanic forms keep working on filtered pages.
- Two bugs were caught by live testing and fixed: a SpringEL `null ||` crash on
  the Services/Bills pages (500 error) — now null-safe `#strings.isEmpty`
  checks — and locale-sensitive `toLowerCase()` — now `Locale.ROOT`.
- **Live-tested: 22/22 checks passed** (each search type, combined filters,
  no-match messages, case-insensitivity, regression on action forms).

## 📊 Added 18 Oct 2026 — Better Reports, README & Database Script
- **Reports page upgraded:** Total Revenue split into **Collected (Paid)** vs
  **Pending Collection**, a **Revenue-by-Month bar chart** (CSS bars, no JS —
  works offline), and a **Services-by-Status chart** with counts & percentages.
- **CSV Export:** Admin → Reports can download Bills / Services / Customers as
  CSV (`/admin/reports/export?type=...`, opens in Excel, properly quoted —
  commas inside descriptions are safe). Admin-only (customers get 403).
- **README.md** rewritten: features by role, tech stack, run instructions,
  default logins, project structure, security notes.
- **database.sql** added: full MySQL schema for all 6 tables (reference/manual
  setup; Hibernate still auto-creates tables normally). Script executed and
  verified against a live database.
- **Live-tested with Aug–Oct 2026 data:** monthly ₹1,000 / ₹2,000 / ₹3,500
  (Oct = ₹3,500 across paid+pending), collected ₹6,000, pending ₹500,
  Completed 2/4 = 50.0%, all 3 CSV exports, content-disposition filenames,
  fallback to bills for unknown type — all passed.

## 🎨 Added 18 Oct 2026 — UI Polish (Demo-Ready Theme v2)
- **New design system** in `style.css`: dark navy/indigo gradient navbar,
  CSS-variable palette, elevated cards with gradient accents, gradient stat
  numbers, zebra/hover tables, pill badges, focus rings, responsive layout,
  reduced-motion support — zero external fonts/CDN (works fully offline).
- **Homepage rebuilt:** hero with trust badge, dual CTAs, stats strip and a
  workshop photo (`static/images/hero-garage.jpg`), "Why Choose AutoCare"
  features, popular services with prices, 4-step How-It-Works, three portal
  cards (Customer/Admin/Mechanic), CTA banner, richer footer.
- **Services page rebuilt** with full nav, 9 priced service cards and CTA.
- **Unified admin navbar** on all 9 admin pages (previously Bills/Reports/
  Services/Contacts/Generate-Bill had no navigation at all).
- **Status pills everywhere:** Pending (amber), Accepted (blue),
  In Progress (violet), Completed/Paid (green), Cancelled (red) — admin,
  customer and mechanic pages.
- **Demo fixes:** customer dashboard now greets by name (was blank —
  `session.userName` was never set); login page's fake customer demo
  credentials replaced with accurate guidance.
- **Verified live:** all 22 pages return 200 with seeded demo data
  (customers Arjun Mehta/Priya Sharma, mechanics Ramesh Kumar/Suresh Patil, services in
  Pending/In Progress/Completed, Paid + Pending bills).

## 🐛 Bug Fix (reported by user) — "Could not commit JPA transaction" on /admin/services
- **Symptom:** Updating status or assigning a mechanic for a service failed with
  "Oops! Something went wrong / Could not commit JPA transaction", and the
  error page's Go Home made it look like the admin was logged out.
- **Root cause (found by us in an earlier change):** `@FutureOrPresent` was placed on the
  JPA entity field `Service.serviceDate`. Hibernate re-validates entities on
  EVERY database update — so once a booking's date passed, that service could
  never be updated again (status changes, mechanic assignment, mechanic job
  updates all failed). Future-dated services worked, which made it confusing.
- **Reproduced live** with a service dated 2025-01-15: both admin actions failed
  exactly as reported; a future-dated control service worked.
- **Fix:** removed `@FutureOrPresent` from the entity; the past-date rule now
  lives only at booking time in `CustomerController.bookService()` via
  `result.rejectValue(...)`. Mechanics/admins can update old bookings freely,
  while new past-date bookings are still rejected.
- **Also:** error page now has a proper **← Go Back** button (+ note that you
  are NOT logged out — the session was actually still alive; the public home
  page just doesn't show it), and `GlobalExceptionHandler` now logs the real
  exception + stack trace to the console.
- **Verified live after fix:** assign mechanic ✓, admin status update ✓,
  mechanic status update ✓ — all on the past-dated service; new past booking
  rejected ✓, today/future bookings still work ✓.

## 🏁 Status: original gap list COMPLETE
All planned features are built and live-tested. Only optional minor clean-ups remain:
1. CSRF is disabled in `SecurityConfig` (fine for demos; enable for production).
2. `UserService` creates its own `BCryptPasswordEncoder` instead of using the Spring bean.
3. Add pagination if data grows large; add automated unit/integration tests.

## ▶️ How to run locally
1. Install JDK 21 + Maven, and MySQL.
2. In MySQL: `CREATE DATABASE vehicle_service_db;`
3. Set your MySQL username/password in `src/main/resources/application.properties`.
4. Run: `mvn spring-boot:run` → open http://localhost:8080
5. Login as admin (`admin@example.com` / `admin123`) or register as a customer.

## 📤 Getting these fixes back to GitHub
These changes exist in the Arena workspace copy of the repo (6 files changed).
Download the changed files from the workspace and copy them into your local project
(same paths), then commit & push with GitHub Desktop — or ask Arena to prepare
the files in another way.
