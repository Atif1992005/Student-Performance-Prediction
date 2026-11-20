@echo off
echo Starting Student Academic Risk Prediction System...

start "FastAPI ML Service" cmd /k "cd C:\Users\atif1\OneDrive\Desktop\stud_ml_proj\ml && python -m uvicorn api:app --host 127.0.0.1 --port 8000"

start "Spring Boot Backend" cmd /k "cd C:\Users\atif1\OneDrive\Desktop\stud_ml_proj\backend && set JAVA_HOME=C:\Program Files\Java\jdk-21&& set MYSQL_USER=root&& set MYSQL_PASSWORD=1234&& mvnw.cmd spring-boot:run"

start "React Frontend" cmd /k "cd C:\Users\atif1\OneDrive\Desktop\stud_ml_proj\frontend && npm run dev"
