# IntelliSure Enterprise — No-Docker Office Laptop Setup Guide

This guide is designed for office laptops where **Docker is not installed and cannot be installed** (due to corporate restrictions, lack of admin rights, or security policies). 

IntelliSure runs natively on **Java 17**, a local/portable **MySQL** database, and **Node.js**.

---

## 1. What You Need on Your Office Laptop

You only need 3 things installed locally:

| Dependency | Required Version | How to Check | If You Don't Have It / No Admin Rights |
|---|---|---|---|
| **Git** | Any modern version | `git --version` | Standard office tool or download portable Git from [git-scm.com](https://git-scm.com/) |
| **Java JDK** | 17 LTS | `java -version` | Download Eclipse Temurin 17 ZIP or Amazon Corretto 17. (You can extract the ZIP to a folder without admin rights and set `JAVA_HOME`). |
| **Node.js & npm** | Node 18 or 20 LTS | `node -v` & `npm -v` | Download Node.js Windows ZIP / macOS binary from [nodejs.org](https://nodejs.org/), extract and add to `PATH`. |
| **MySQL Database** | MySQL 8.x or 8.4 (or MariaDB) | `mysql --version` | See **Section 3** for installing or running portable MySQL without admin rights. |

---

## 2. Clone the Repository

Open your terminal (PowerShell on Windows, Terminal / Zsh on macOS):

```bash
git clone https://github.com/gouthamlingojuhf/Intellisure.git
cd Intellisure
git checkout feature/chat-system
git pull origin feature/chat-system
```

---

## 3. Database Setup (No Docker)

All 11 microservices connect to MySQL on `localhost:3306`.

### Option A: You already have MySQL Server installed
If MySQL is already running on your machine:
1. Open PowerShell or Command Prompt.
2. Run the database initialization and seed scripts:

**Windows (PowerShell):**
```powershell
# 1. Create the 9 microservice databases
Get-Content docker/mysql/init/00-create-databases.sql | mysql -u root -p

# 2. Seed all roles, demo accounts, and accredited vendors
Get-Content docker/mysql/init/reset_and_seed.sql | mysql -u root -p
```

**macOS / Linux:**
```bash
# 1. Create the 9 microservice databases
mysql -u root -p < docker/mysql/init/00-create-databases.sql

# 2. Seed all roles, demo accounts, and accredited vendors
mysql -u root -p < docker/mysql/init/reset_and_seed.sql
```

*(Enter your local MySQL root password when prompted).*

### Option B: If you do NOT have MySQL installed & have NO admin rights
You can use any of these lightweight, non-Docker solutions:
1. **XAMPP Portable (No Admin Required)**:
   - Download XAMPP Portable `.zip` from [apachefriends.org](https://www.apachefriends.org/).
   - Extract to `C:\xampp` and run `xampp-control.exe`.
   - Click **Start** next to MySQL. It runs immediately on port 3306 with user `root` and blank password!
2. **MySQL Noinstall / ZIP Archive**:
   - Download the MySQL Community ZIP from MySQL official site.
   - Extract to a folder (e.g. `C:\mysql-8.4`).
   - Run `bin\mysqld.exe --console` to start the database server.
3. Once running, execute the two SQL scripts from Option A above.

### Custom MySQL Password Configuration
By default, the backend services connect to `localhost:3306` with user `root` and **blank password** (`password: `).
If your local MySQL root user has a password (e.g., `MyPassword123`), you **do not** need to edit any files. Simply set this environment variable in your terminal before running:

**Windows (PowerShell):**
```powershell
$env:SPRING_R2DBC_PASSWORD="MyPassword123"
```

**macOS / Linux:**
```bash
export SPRING_R2DBC_PASSWORD="MyPassword123"
```

---

## 4. Build All Backend Services (One Command)

Every service directory contains its own self-contained Maven Wrapper (`mvnw.cmd` / `./mvnw`), so you **do not** need Maven installed.

### On Windows:
```powershell
.\build_all.ps1
```

### On macOS / Linux:
```bash
./build_all.sh
```

This compiles all 11 Spring Boot services in sequence (`eureka`, `api-gateway`, and the 9 domain microservices) and creates executable JARs in each service's `target/` directory.

---

## 5. Start All Backend Services (One Command)

### On Windows (PowerShell):
```powershell
.\run_all_services.ps1
```
*What this does:*
- Opens separate terminal windows for each service so you can monitor logs.
- Starts `eureka` (Port 8761) first and waits for it to be ready.
- Starts `api-gateway` (Port 8080).
- Starts all 9 microservices (Ports 8081–8089) with memory-safe heap limits (`-Xms128m -Xmx256m`) to prevent high RAM usage.

### On macOS / Linux:
```bash
./run_all_services.sh
```
*What this does:*
- Starts all 11 services in the background and writes their process IDs to `.running_services.pid`.

### How to Stop the Backend Services:
When you are done testing, run:
- **Windows:** `.\stop_all_services.ps1`
- **macOS / Linux:** `./stop_all_services.sh`

---

## 6. Frontend Setup & Launch

The frontend requires only Node.js and npm (no Docker needed).

### Step 6.1: Install Dependencies
In your terminal, navigate to the `frontend/` directory:
```bash
cd frontend
npm install
```

### Step 6.2: Build Shared Libraries (MANDATORY STEP)
```bash
npm run build:libs
```
> [!IMPORTANT]
> This compiles `@intellisure/ui-core` into `dist/ui-core`. All microfrontends depend on this library. You only need to run this once.

### Step 6.3: Start Frontend Dev Servers
Run all 5 frontend applications simultaneously:
```bash
npm run run:all
```

*This starts:*
- **Shell Application** on `http://localhost:4200`
- **Auth MFE** on `http://localhost:4201`
- **Claims MFE** on `http://localhost:4202`
- **Intelligence MFE** on `http://localhost:4203`
- **Vendor MFE** on `http://localhost:4205`

---

## 7. Open the App in Your Browser

Open your browser and navigate to:
👉 **[http://localhost:4200](http://localhost:4200)**

You can also check:
- **Eureka Service Registry**: [http://localhost:8761](http://localhost:8761) (Lists all 11 registered microservices).
- **API Gateway**: [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health) (Shows `{"status":"UP"}`).
- **In-App Platform Guide**: [http://localhost:4200/docs/guide](http://localhost:4200/docs/guide).

---

## 8. Pre-Seeded Demo Accounts & Credentials

All seeded demo accounts are ready to use with the standard password:
👉 **`Password@123`** *(or `Admin@123` for administrator)*

| Role | Email | Password | Primary Functions |
|---|---|---|---|
| **Policyholder** | `owner@restaurant.com`<br/>*(or register fresh via `/login`)* | `Password@123` | Request Commercial Quotes (with Asset Valuations in ₹), Bind Policies, File Claims in INR, Chat with staff. |
| **Underwriter** | `underwriter1@intellisure.com`<br/>`underwriter2@intellisure.com` | `Password@123` | Assigned Quotes Queue, View Applicant Business Profile, Review Risk Scores, Rate & Issue Quotes. |
| **Risk Engineer** | `riskengineer1@intellisure.com`<br/>`riskengineer2@intellisure.com` | `Password@123` | Lands on `/quotes` without access denied, Conduct Structural Risk Surveys, Compute Hazard Scores. |
| **Claims Adjuster** | `claimsadjuster1@intellisure.com`<br/>`claimsadjuster2@intellisure.com` | `Password@123` | Assigned Claims Queue, Record Technical Findings, Transition Claims to `ADJUSTER_REVIEWED`. |
| **Claims Manager** | `claimsmanager1@intellisure.com`<br/>`claimsmanager2@intellisure.com` | `Password@123` | Inspect Adjuster Technical Findings, Prefilled Settlement Governance, Dispatch Network Vendors, Subrogation. |
| **Network Vendor** | `vendor1@intellisure.com`<br/>`vendor2@intellisure.com` | `Password@123` | View assigned work orders without 403, submit invoices, direct chat with Vendor Manager. |
| **Vendor Manager** | `vendormanager1@intellisure.com`<br/>`vendormanager2@intellisure.com` | `Password@123` | Accredited vendor directory, dispatch restoration requests, chat with vendors. |
| **System Admin** | `admin1@intellisure.com`<br/>`admin@intellisure.com` | `Password@123`<br/>`Admin@123` | Dynamic Reassignment of Underwriters and Risk Engineers, Audit Logs, Portfolio Loss Ratio Metrics. |

---

## 9. Office Laptop Troubleshooting

1. **Corporate SSL Inspection Error during `npm install`**:
   ```bash
   npm config set strict-ssl false
   ```
2. **Port 8080 Conflict (e.g. Jenkins, Tomcat, or proxy on 8080)**:
   - Check what is running: `netstat -ano | findstr :8080` (Windows) or `lsof -i :8080` (macOS).
   - If a corporate service is using port 8080, you can adjust `server.port: 8080` in `api-gateway/src/main/resources/application.yaml` and update the proxy port.
3. **RAM Optimization for 16 GB Laptops**:
   - The startup script `run_all_services.ps1` already limits each JVM to `-Xms128m -Xmx256m`. Total RAM for all 11 Java services is under 2.8 GB!
4. **Stopping all services**:
   - Simply run `.\stop_all_services.ps1` (Windows) or `./stop_all_services.sh` (macOS) to close all background processes cleanly.
