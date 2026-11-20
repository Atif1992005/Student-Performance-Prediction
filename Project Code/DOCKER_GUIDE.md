# 🐳 Docker Deployment & Portability Guide

> **Student Academic Risk Prediction System**  
> Complete multi-container architecture packaging React (Vite/Nginx), Spring Boot (Java 21), Python FastAPI (Machine Learning), and MySQL 8.0.

---

## 📋 Table of Contents
1. [Overview & Architecture](#1-overview--architecture)
2. [Prerequisites](#2-prerequisites)
3. [Mode A: Production / Portable Execution (Recommended)](#3-mode-a-production--portable-execution-recommended)
4. [Mode B: Live Development Mode (Hot Reload)](#4-mode-b-live-development-mode-hot-reload)
5. [Database Persistence & Seed Data](#5-database-persistence--seed-data)
6. [Offline Image Export & Transfer to Another PC](#6-offline-image-export--transfer-to-another-pc)
7. [Running Without Docker (Existing Method)](#7-running-without-docker-existing-method)
8. [Troubleshooting & Port Conflicts](#8-troubleshooting--port-conflicts)

---

## 1. Overview & Architecture

This project is containerized into four isolated, communicating microservices managed by `compose.yaml`:

| Service | Technology | Internal Port | Host Port | Description |
| :--- | :--- | :--- | :--- | :--- |
| **`frontend`** | React + Vite / Nginx | `80` | `5173` | Serves the web UI & reverse-proxies `/api` to backend |
| **`backend`** | Spring Boot (Java 21) | `8080` | `8080` | REST API, JPA persistence, Auth, ML client |
| **`ml-service`**| Python 3.11 + FastAPI | `8000` | `8000` | Supervised Logistic Regression & RF risk models |
| **`mysql`** | MySQL 8.0 Engine | `3306` | `3306` | Persistent database seeded with all 66 students |

All services communicate through a private Docker bridge network (`student_risk_network`).

---

## 2. Prerequisites

### On a Target PC (Running via Docker):
- **Only Docker Desktop or Docker Engine** (with Compose v2) is needed.
- ❌ **NO need to install**: Java, Maven, Python, scikit-learn, Node.js, npm, or MySQL on the host machine. Everything runs self-contained inside the containers.

---

## 3. Mode A: Production / Portable Execution (Recommended)

In production mode, each container builds an optimized, self-contained image ready for distribution.

### 🚀 Build and Start All Services
```bash
docker compose up -d --build
```

### 🌐 Accessing the Application
- **Student & Faculty Web Portal**: [http://localhost:5173](http://localhost:5173)
- **Spring Boot API**: [http://localhost:8080/api/students](http://localhost:8080/api/students)
- **FastAPI Model Health & Swagger Docs**: [http://localhost:8000/docs](http://localhost:8000/docs)

### 🔑 Demo Login Credentials
- **Faculty Advisor**: Username: `F0001` \| Password: `password123`
- **Student Profile**: Roll No: `101` (Rutuja Deshmukh) \| Password: `password123`

### 🛑 Stopping Services
```bash
# Stop containers (preserves database records in named volume)
docker compose down

# Stop containers AND wipe database volume (fresh clean reset)
docker compose down -v
```

---

## 4. Mode B: Live Development Mode (Hot Reload)

If you are developing features and want live source-code reloading (changes in React or Python reflect immediately without rebuilding Docker images):

```bash
docker compose -f compose.yaml -f compose.dev.yaml up
```

- **Frontend**: Mounts `./frontend` into the container with Vite HMR on port `5173`.
- **ML Service**: Mounts `./ml` into the container with Uvicorn `--reload` on port `8000`.

---

## 5. Database Persistence & Seed Data

- **Automatic Pre-Seeding**: On first startup, MySQL runs `docker/mysql/init.sql`, populating all **66 enrolled students** (Roll Nos 101–166) and auth credentials.
- **Persistent Volume**: All data, student records added via UI, and profile modifications are stored in the named Docker volume:
  ```text
  student_risk_mysql_data
  ```
  Data remains safe across `docker compose restart`, `docker compose down`, or PC restarts.

---

## 6. Offline Image Export & Transfer to Another PC

To transfer this application to another PC that has **no internet access** or where you don't want to rebuild dependencies:

### Step 1: Build the images on your machine
```bash
docker compose build
```

### Step 2: Export images to a single archive file (`.tar`)
```bash
docker save \
  student-academic-risk-frontend:latest \
  student-academic-risk-backend:latest \
  student-academic-risk-ml:latest \
  mysql:8.0 \
  -o student_academic_risk_images.tar
```

### Step 3: Copy files to the target PC
Copy the following files to a folder on the target PC (e.g., via USB flash drive):
1. `student_academic_risk_images.tar`
2. `compose.yaml`
3. `.env` (or copy `.env.example` as `.env`)
4. The `docker/mysql/init.sql` file (for initial database seeding)

### Step 4: Load images on the target PC
Open a terminal in the folder on the target PC:
```bash
docker load -i student_academic_risk_images.tar
```

### Step 5: Start the application
```bash
docker compose up -d
```
All four services will start immediately using the loaded local images without downloading a single byte from the internet!

---

## 7. Running Without Docker (Existing Method)

Docker operates completely independently. You can continue running the project locally as before:

```cmd
:: Using the existing batch script:
start_project.bat
```

Or manually:
1. **ML Service**: `cd ml && python -m uvicorn api:app --host 127.0.0.1 --port 8000`
2. **Spring Boot**: `cd backend && mvnw.cmd spring-boot:run`
3. **Frontend**: `cd frontend && npm run dev`

---

## 8. Troubleshooting & Port Conflicts

### Port 3306 Already in Use on Host
If your host PC already runs a local MySQL service (e.g. `MySQL80` service), the Docker MySQL container will report a port conflict.
**Solution**: Edit `.env` and set:
```env
MYSQL_PORT=3307
```
This maps Docker's internal MySQL to port `3307` on the host, eliminating any conflict while internal container-to-container communication remains on port `3306`.

### Checking Service Status & Logs
```bash
# Check status of all containers
docker compose ps

# View live log outputs across all services
docker compose logs -f

# View logs for a specific service
docker compose logs -f backend
docker compose logs -f ml-service
docker compose logs -f frontend
docker compose logs -f mysql
```
