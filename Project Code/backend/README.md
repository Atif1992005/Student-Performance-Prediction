# Student Academic Risk — Spring Boot backend (Stage 5)

Stores the 10 ML academic features in MySQL and integrates with the FastAPI ML service (`http://127.0.0.1:8000`) for real-time risk prediction.

## Configuration

Set environment variables if different from default:

```bash
set MYSQL_USER=root
set MYSQL_PASSWORD=your_mysql_password
set ML_SERVICE_BASE_URL=http://127.0.0.1:8000
```

## Run

```bash
cd backend
set JAVA_HOME=C:\Program Files\Java\jdk-21
mvnw.cmd spring-boot:run
```

API base: `http://localhost:8080`

## Endpoints

### Student CRUD
- `GET /api/students` — List all saved students
- `GET /api/students/{id}` — Get single student by ID
- `POST /api/students` — Create a new student (JSON with 10 features)
- `PUT /api/students/{id}` — Update existing student
- `DELETE /api/students/{id}` — Delete student

### Risk Prediction
- `POST /api/predict` — Predict risk from ad-hoc 10 features JSON payload
- `POST /api/students/{id}/predict` — Predict risk for a saved student record in MySQL
- `GET /api/students/{id}/predict` — Predict risk for a saved student record in MySQL (GET convenience)

### Example `POST /api/predict` or `POST /api/students`:

```json
{
  "CGPA": 7.35,
  "Last_Semester_SGPA": 6.27,
  "Backlogs": 2,
  "Previous_Semester_Internal_Marks": 19.6,
  "Previous_Semester_Attendance": 66.3,
  "Previous_Semester_Assignment_Score": 54.7,
  "Previous_Semester_Test_Score": 62.1,
  "Current_Semester_Attendance": 61.9,
  "Current_Semester_Test_Score": 41.0,
  "Assignment_Completion_Percent": 20.0
}
```

### Example Prediction Response:

```json
{
  "Academic_Risk": "Medium",
  "probabilities": {
    "Low": 0.012666,
    "Medium": 0.753753,
    "High": 0.233582
  },
  "model": "Logistic Regression",
  "comparison": {
    "model": "Random Forest",
    "Academic_Risk": "Medium",
    "probabilities": {
      "Low": 0.04,
      "Medium": 0.875,
      "High": 0.085
    }
  }
}
```

