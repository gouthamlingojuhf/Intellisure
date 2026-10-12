# IntelliSure Enterprise — Office Laptop Setup & Deployment Guide

This document provides a comprehensive, step-by-step runbook for setting up and running the complete **IntelliSure** enterprise insurance platform on a new office laptop (Windows, macOS, or Linux).

---

## 1. System Requirements & Prerequisites

### 1.1 Hardware Requirements
- **RAM**: 16 GB minimum (32 GB recommended for running all Docker containers and Angular MFEs concurrently).
- **CPU**: 4 cores / 8 threads minimum (Intel i5/i7/i9, AMD Ryzen, Apple Silicon M1/M2/M3/M4).
- **Free Disk Space**: 25 GB free storage (for Docker images, MySQL volumes, and `node_modules`).

### 1.2 Required Software Tools
Install the following software before proceeding:

| Tool | Recommended Version | Verification Command | Notes / Download Link |
|---|---|---|---|
| **Git** | 2.40+ | `git --version` | [git-scm.com](https://git-scm.com/) |
| **Docker Desktop** | 4.25+ | `docker --version` & `docker compose version` | [docker.com](https://www.docker.com/products/docker-desktop/)<br/>*(Windows: Ensure WSL2 backend is enabled)* |
| **Java JDK** | 17 LTS | `java -version` | Eclipse Temurin 17 or Amazon Corretto 17 |
| **Node.js** | 18 LTS or 20 LTS | `node -v` | [nodejs.org](https://nodejs.org/) (v18.20+ or v20.12+) |
| **npm** | 9+ or 10+ | `npm -v` | Bundled with Node.js |

---

## 2. Clone the Repository & Checkout Branch

Open your terminal (PowerShell / Command Prompt on Windows, Terminal / Zsh on macOS / Linux):

```bash
# Clone the official repository
git clone https://github.com/gouthamlingojuhf/Intellisure.git

# Enter project root directory
cd Intellisure

# Checkout the feature branch containing the complete system implementation
git checkout feature/chat-system

# Ensure your working tree is updated
git pull origin feature/chat-system
```

---

## 3. Option A: Run Full Stack via Docker (Recommended)

Docker Compose orchestrates the entire backend mesh:
- 1 MySQL 8.4 Server
- 1 Netflix Eureka Service Registry
- 1 Spring Cloud Reactive API Gateway
- 9 Reactive Spring WebFlux Microservices

### Step 3.1: Configure Environment Variables
Create a local `.env` file in the root `Intellisure/` folder (or export the variables in your terminal session):

**Windows (PowerShell):**
```powershell
$env:MYSQL_ROOT_PASSWORD="RootPassword@123"
$env:INTELLISURE_JWT_SECRET="404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
```

**macOS / Linux (Bash / Zsh):**
```bash
export MYSQL_ROOT_PASSWORD='RootPassword@123'
export INTELLISURE_JWT_SECRET='404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970'
```

Alternatively, create an untracked `.env` file in the repository root:
```properties
MYSQL_ROOT_PASSWORD=RootPassword@123
INTELLISURE_JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

### Step 3.2: Build and Start Containers
From the repository root directory, run:

```bash
# Build all backend microservice JARs and start the container stack in detached mode
docker compose up --build -d
```

### Step 3.3: Verify Container Health
Wait approximately 45–60 seconds for all Spring Boot services to perform Eureka healthchecks. Run:

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
```

You should see all 12 containers running and reporting `(healthy)`:
- `intellisure-mysql-1` (Port 3306)
- `intellisure-eureka-server-1` (Port 8761)
- `intellisure-customer-party-service-1` (Port 8081)
- `intellisure-quote-policy-service-1` (Port 8082)
- `intellisure-risk-underwriting-service-1` (Port 8083)
- `intellisure-claims-service-1` (Port 8084)
- `intellisure-vendor-partner-service-1` (Port 8085)
- `intellisure-recovery-service-1` (Port 8086)
- `intellisure-workflow-notification-service-1` (Port 8087)
- `intellisure-document-audit-service-1` (Port 8088)
- `intellisure-analytics-intelligence-service-1` (Port 8089)
- `intellisure-api-gateway-1` (Port 8080)

### Step 3.4: Seed / Reset Demo Database Records
The Docker initialization script automatically seeds databases on first run. If you ever need to reset to a clean state with fresh demo accounts:

**Windows (PowerShell):**
```powershell
Get-Content docker/mysql/init/reset_and_seed.sql | docker exec -i intellisure-mysql-1 mysql -uroot -pRootPassword@123
```

**macOS / Linux:**
```bash
docker exec -i intellisure-mysql-1 mysql -uroot -pRootPassword@123 < docker/mysql/init/reset_and_seed.sql
```

---

## 4. Option B: Run Backend Locally Without Docker

If corporate security policies prohibit Docker on your office laptop:

1. **Install MySQL 8.4 locally** on default port `3306` with root password `RootPassword@123`.
2. **Execute Database Setup**:
   Execute `docker/mysql/init/reset_and_seed.sql` against your local MySQL instance.
3. **Build All Backend Modules**:
   ```powershell
   # Windows PowerShell:
   ./build_all.ps1
   ```
4. **Launch Backend Services in Order**:
   - `eureka`: `cd eureka && ./mvnw spring-boot:run` (Wait until UP on port 8761)
   - `customer-party-service`: `cd customer-party-service && ./mvnw spring-boot:run`
   - `quote-policy-service`: `cd quote-policy-service && ./mvnw spring-boot:run`
   - `risk-underwriting-service`: `cd risk-underwriting-service && ./mvnw spring-boot:run`
   - `claims-service`: `cd claims-service && ./mvnw spring-boot:run`
   - `vendor-partner-service`: `cd vendor-partner-service && ./mvnw spring-boot:run`
   - `recovery-service`: `cd recovery-service && ./mvnw spring-boot:run`
   - `workflow-notification-service`: `cd workflow-notification-service && ./mvnw spring-boot:run`
   - `document-audit-service`: `cd document-audit-service && ./mvnw spring-boot:run`
   - `analytics-intelligence-service`: `cd analytics-intelligence-service && ./mvnw spring-boot:run`
   - `api-gateway`: `cd api-gateway && ./mvnw spring-boot:run` (Port 8080)

*(Note: On Windows, running `./run_all_services.ps1` automatically launches all services in separate terminals).*

---

## 5. Frontend Setup & Execution

The frontend consists of an Angular 17 Module Federation Shell and 4 Microfrontends (MFEs).

### Step 5.1: Install Dependencies
Navigate into the `frontend/` directory and install npm packages:

```bash
cd frontend
npm install
```

### Step 5.2: Build Shared Libraries (CRITICAL STEP)
Before launching any Angular application, you **MUST** compile the shared design-system library (`ui-core`):

```bash
npm run build:libs
```
> [!IMPORTANT]
> Failure to run `npm run build:libs` will cause compilation errors because the shell and microfrontends import design system tokens from `@intellisure/ui-core`.

### Step 5.3: Start Frontend Dev Servers
You can start all microfrontends simultaneously:

```bash
npm run run:all
```

Alternatively, you can open individual terminal windows in `frontend/` and launch them separately:
```bash
# Terminal 1: Shell Application (Port 4200)
npm run start:shell

# Terminal 2: Auth Microfrontend (Port 4201)
npm run start:auth

# Terminal 3: Claims Microfrontend (Port 4202)
npm run start:claims

# Terminal 4: Intelligence & Analytics Microfrontend (Port 4203)
npm run start:intelligence

# Terminal 5: Vendor Partner Microfrontend (Port 4205)
npm run start:vendor
```

---

## 6. Access Points & Verification Table

Once all services are started, verify access in your browser:

| Application / Service | URL | Default Port | Expected Behavior |
|---|---|---|---|
| **IntelliSure Web Application** | [http://localhost:4200](http://localhost:4200) | `4200` | Landing Page & Unified Shell |
| **Eureka Service Registry** | [http://localhost:8761](http://localhost:8761) | `8761` | Eureka Dashboard listing all 10 registered instances |
| **API Gateway Health** | [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) | `8080` | `{"status":"UP"}` |
| **Platform User Guide** | [http://localhost:4200/docs/guide](http://localhost:4200/docs/guide) | `4200` | Complete Role Guides & Walkthroughs |

---

## 7. Pre-Seeded Demo Credentials

All test accounts in the platform have been pre-configured with the standard password:
👉 **`Password@123`** *(or `Admin@123` for administrators)*

| Role | Email Address | Password | Primary Capabilities & Workflows |
|---|---|---|---|
| **`POLICYHOLDER`** | `owner@restaurant.com` *(or register fresh)* | `Password@123` | Create Quotes with Property Valuations (₹), Bind Policies, File FNOL Claims, Dispatch Vendor Requests, Peer Chat. |
| **`UNDERWRITER`** | `underwriter1@intellisure.com`<br/>`underwriter2@intellisure.com` | `Password@123` | Inspect assigned quotes, view applicant business profile, review risk scores, rate and approve policies in INR (₹). |
| **`RISK_ENGINEER`** | `riskengineer1@intellisure.com`<br/>`riskengineer2@intellisure.com` | `Password@123` | Direct landing on `/quotes` without access denied, property survey evaluations, structural risk hazard scoring. |
| **`CLAIMS_ADJUSTER`** | `claimsadjuster1@intellisure.com`<br/>`claimsadjuster2@intellisure.com` | `Password@123` | Field investigations, technical assessment persistence, coverage decision submission, transitions claims to `ADJUSTER_REVIEWED`. |
| **`CLAIMS_MANAGER`** | `claimsmanager1@intellisure.com`<br/>`claimsmanager2@intellisure.com` | `Password@123` | Inspect adjuster technical findings, pre-filled editable settlement governance, dispatch network vendor requests, subrogation recovery. |
| **`VENDOR`** | `vendor1@intellisure.com`<br/>`vendor2@intellisure.com` | `Password@123` | Direct assignment queue without 403, work order execution, invoice submission, direct chat with Vendor Manager. |
| **`VENDOR_MANAGER`**| `vendormanager1@intellisure.com`<br/>`vendormanager2@intellisure.com` | `Password@123` | Vendor directory accreditation, recovery dispatch requests, direct messaging with network vendors. |
| **`ADMIN`** | `admin1@intellisure.com`<br/>`admin@intellisure.com` | `Password@123`<br/>`Admin@123` | Reassign Underwriters & Risk Engineers dynamically from live DB, system telemetry, audit trail inspection. |

---

## 8. Common Office Laptop Gotchas & Troubleshooting

### 1. Corporate VPN / SSL Inspection Errors during `npm install`
* **Symptom**: `npm ERR! self-signed certificate in certificate chain`
* **Fix**: Temporarily configure npm to use your corporate CA or disable strict SSL for development:
  ```bash
  npm config set strict-ssl false
  ```

### 2. Port Conflicts (e.g., Port 8080 or 3306 already in use)
* **Symptom**: `Bind for 0.0.0.0:8080 failed: port is already allocated`
* **Fix**: Check which local software is occupying the port:
  - **Windows**: `netstat -ano | findstr :8080` $\rightarrow$ Stop process using Task Manager or `taskkill /PID <PID> /F`.
  - **macOS**: `lsof -i :8080` $\rightarrow$ `kill -9 <PID>`.
  - *If you have a local MySQL service running on Windows, stop it via Services app (`services.msc`) so Docker can bind port 3306.*

### 3. Docker Desktop RAM & CPU Allocation
* **Symptom**: Spring Boot containers crash or exit with error 137 (OOMKilled).
* **Fix**:
  - Open **Docker Desktop Settings $\rightarrow$ Resources**.
  - Set Memory to at least **8 GB** (12 GB or 16 GB recommended).
  - Set CPUs to at least **4**.
  - On Windows with WSL2, create or edit `%USERPROFILE%\.wslconfig`:
    ```ini
    [wsl2]
    memory=12GB
    processors=6
    ```

### 4. Maven Wrapper Script Permissions (macOS / Linux)
* **Symptom**: `zsh: permission denied: ./mvnw`
* **Fix**: Run `chmod +x ./mvnw` in the service directory (e.g. `chmod +x */mvnw`).

### 5. Frontend Missing Module or UI Components
* **Symptom**: `Cannot find module '@intellisure/ui-core'` during `ng serve` or `npm run run:all`.
* **Fix**: You must build the shared library first:
  ```bash
  cd frontend
  npm run build:libs
  ```
