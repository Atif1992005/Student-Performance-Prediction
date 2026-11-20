# Student Academic Risk Prediction System

An end-to-end full-stack Machine Learning application designed to identify students at academic risk early in the semester, enabling timely educational interventions and automated remediation advisories.

---

## Architecture Overview

The system uses a decoupled, multi-tier microservice architecture:

```
┌─────────────────────────────────┐
│     React 18 + Vite Frontend    │ (Port 5173)
│   Interactive Dashboard & UI    │
└────────────────┬────────────────┘
                 │ REST / JSON
                 ▼
┌─────────────────────────────────┐
│    Spring Boot 3.4 Backend      │ (Port 8080)
│  Student CRUD & Business Logic  │
└────────┬───────────────┬────────┘
         │ JPA           │ REST Client
         ▼               ▼
┌─────────────────┐  ┌───────────────────────────────────┐
│   MySQL 8.0     │  │      Python FastAPI ML Service    │ (Port 8000)
│ Student Records │  │ Logistic Regression (predict_proba)│
└─────────────────┘  └───────────────────────────────────┘
```

- **Frontend (`frontend/`)**: Modern React 18 single-page application built with Vite, featuring responsive academic dashboards, real-time risk gauges, and departmental threshold configuration.
- **Backend (`backend/`)**: Java 21 / Spring Boot 3 enterprise REST API managing student profiles, database persistence via Spring Data JPA / Hibernate, and coordinating predictions.
- **ML Service (`ml/`)**: High-performance Python FastAPI service serving a trained Scikit-Learn pipeline (StandardScaler + Logistic Regression) with real-time class probabilities and Random Forest cross-validation comparison.
- **Database**: MySQL relational database storing comprehensive student academic profiles across 10 predictive features.

---

## Key Features

- **Multi-Class Academic Risk Classification**: Predicts **Low**, **Medium**, or **High** academic risk based on holistic academic performance.
- **Calibrated Prediction Confidence**: Displays true class probabilities directly from Scikit-Learn's `predict_proba()` with 1-decimal-place precision (e.g., `99.7%`, `75.4%`).
- **Dynamic Reasons & Academic Recommendations**: Contextual diagnosis based on customizable institutional thresholds:
  - *Attendance Warnings*: Detention alerts for attendance $< 40\%$, shortage warnings for attendance $< 60\%$.
  - *Backlog Warnings*: Promotion alerts for active backlogs $> 5$.
  - *CGPA Guidance*: Remediation goals for CGPA $< 6.0$, maintenance advisories for CGPA $6.0 - 7.0$.
  - *Assignment Alerts*: Follow-up flags when assignment completion $< 40\%$.
- **Configurable Department Policy**: Academic coordinators can adjust risk thresholds in real-time through the UI without code changes.
- **Student Profile Management**: Complete CRUD operations for student records.

---

## 10 Predictive Academic Features

| # | Feature Name | Description | Scale / Unit |
|---|--------------|-------------|--------------|
| 1 | `CGPA` | Cumulative Grade Point Average | 0.0 – 10.0 |
| 2 | `Last_Semester_SGPA` | Semester Grade Point Average of previous term | 0.0 – 10.0 |
| 3 | `Backlogs` | Number of active uncleared backlogs | Count (0 – 20) |
| 4 | `Previous_Semester_Internal_Marks` | Internal assessment score from prior term | Marks (0 – 40) |
| 5 | `Previous_Semester_Attendance` | Overall attendance percentage in previous term | 0% – 100% |
| 6 | `Previous_Semester_Assignment_Score` | Graded assignment performance in prior term | Marks (0 – 100) |
| 7 | `Previous_Semester_Test_Score` | Midterm / terminal test score in prior term | Marks (0 – 100) |
| 8 | `Current_Semester_Attendance` | Real-time attendance rate for ongoing term | 0% – 100% |
| 9 | `Current_Semester_Test_Score` | Recent internal examination score | Marks (0 – 100) |
| 10 | `Assignment_Completion_Percent` | Current percentage of submitted assignments | 0% – 100% |

---

## Tech Stack

- **Frontend**: React 18, Vite, Lucide React, Modern CSS3
- **Backend**: Java 21 / 17, Spring Boot 3.4.5, Spring Data JPA, Hibernate, Maven
- **Machine Learning**: Python 3.10+, Scikit-Learn, FastAPI, Uvicorn, Pandas, NumPy, Joblib
- **Database**: MySQL 8.0+

---

## Project Structure

```
stud_ml_proj/
├── backend/                  # Spring Boot REST API
│   ├── src/main/java/        # Controllers, Services, Entities, Repositories
│   ├── src/main/resources/   # application.properties & database configuration
│   ├── pom.xml               # Maven dependencies
│   └── mvnw, mvnw.cmd        # Maven wrapper scripts
├── frontend/                 # React + Vite application
│   ├── src/components/       # UI components (StudentList, PredictionCard, RiskBadge, etc.)
│   ├── src/utils/            # Configurable threshold evaluation logic
│   ├── package.json          # Node dependencies & scripts
│   └── vite.config.js        # Vite build configuration
├── ml/                       # Machine Learning service
│   ├── api.py                # FastAPI endpoints (/predict, /health)
│   ├── models/               # Serialized trained model artifacts (.joblib)
│   ├── reports/              # Model evaluation metrics & confusion matrices
│   ├── student_risk/         # ML pipeline, data loader, trainer, and serve bundle
│   ├── requirements.txt      # Python dependencies
│   └── run_api.py            # API launcher script
├── student_academic_risk_dataset_20000.csv # 20k dataset for model training
├── .gitignore                # Production git ignore configuration
└── README.md                 # Project documentation
```

---

## Getting Started & Local Setup

### Prerequisites

Ensure the following tools are installed on your machine:
- **Node.js** (v18+) & **npm**
- **Java JDK** (17 or 21)
- **Python** (3.10+)
- **MySQL Server** (8.0+)

---

### Step 1: Database Setup (MySQL)

1. Start your local MySQL service.
2. Create the database:
   ```sql
   CREATE DATABASE student_academic_risk;
   ```
3. Set your environment variables for Spring Boot credentials (or pass them when launching):
   ```bash
   # Windows (CMD / PowerShell)
   set MYSQL_USER=root
   set MYSQL_PASSWORD=your_mysql_password

   # macOS / Linux
   export MYSQL_USER=root
   export MYSQL_PASSWORD=your_mysql_password
   ```

---

### Step 2: Start the Python FastAPI ML Service

1. Navigate to the `ml` directory:
   ```bash
   cd ml
   ```
2. Create and activate a Python virtual environment (optional but recommended):
   ```bash
   python -m venv venv
   # On Windows:
   .\venv\Scripts\activate
   # On Linux/macOS:
   source venv/bin/activate
   ```
3. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
4. Start the FastAPI inference service:
   ```bash
   python run_api.py
   ```
   The ML service will be available at:
   - **Service Endpoint**: `http://127.0.0.1:8000`
   - **Interactive API Docs (Swagger)**: `http://127.0.0.1:8000/docs`

---

### Step 3: Start the Spring Boot Backend

1. Open a new terminal and navigate to `backend`:
   ```bash
   cd backend
   ```
2. Run the application using the Maven wrapper:
   ```bash
   # Windows
   mvnw.cmd spring-boot:run

   # macOS / Linux
   ./mvnw spring-boot:run
   ```
   The backend will start at `http://localhost:8080`.
   - **Students API**: `http://localhost:8080/api/students`
   - **Prediction API**: `http://localhost:8080/api/students/{id}/predict`

---

### Step 4: Start the React Frontend

1. Open a new terminal and navigate to `frontend`:
   ```bash
   cd frontend
   ```
2. Install frontend dependencies:
   ```bash
   npm install
   ```
3. Start the Vite development server:
   ```bash
   npm run dev
   ```
4. Open your browser and navigate to:
   ```
   http://localhost:5173
   ```

---

## Machine Learning Pipeline (Optional Re-training)

To retrain the models from the dataset:

1. **Stage 1 (Data Validation & EDA)**:
   ```bash
   python ml/run_stage1.py
   ```
2. **Stage 2 (Model Training & Comparison)**:
   ```bash
   python ml/run_stage2.py
   ```
3. **Stage 3 (Export Serving Artifacts)**:
   ```bash
   python ml/run_stage3.py
   ```

---

## License

This project is licensed under the MIT License.
