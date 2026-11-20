from pathlib import Path

PROJECT_ROOT = Path(__file__).resolve().parents[2]
ML_ROOT = Path(__file__).resolve().parents[1]

RAW_DATA_PATH = PROJECT_ROOT / "new_student_academic_risk_dataset_categorical_assignments.csv"

PROCESSED_DIR = ML_ROOT / "data" / "processed"
PREPARED_DATA_PATH = PROCESSED_DIR / "features_and_target.csv"
MODELS_DIR = ML_ROOT / "models"
PRIMARY_MODEL_NAME = "Logistic Regression"
COMPARISON_MODEL_NAME = "Random Forest"
PRIMARY_MODEL_PATH = MODELS_DIR / "primary_logistic_regression.joblib"
COMPARISON_MODEL_PATH = MODELS_DIR / "comparison_random_forest.joblib"
SERVING_METADATA_PATH = MODELS_DIR / "serving_metadata.json"
REPORTS_DIR = ML_ROOT / "reports"
FIGURES_DIR = REPORTS_DIR / "figures"

RANDOM_STATE = 42
TEST_SIZE = 0.20

TARGET_COLUMN = "Academic_Risk"

ASSIGNMENT_CATEGORIES = ["< 30%", "≥ 30% and < 60%", "≥ 60%"]
FEATURE_CATEGORY_LEVELS = ["Low", "Medium", "High"]

FEATURE_COLUMNS = [
    "Attendance_Percent",
    "CGPA",
    "Backlogs",
    "Assignments_Percent",
    "Average_Test_Score_Percent",
]

CATEGORICAL_FEATURE_COLUMNS = list(FEATURE_COLUMNS)
NUMERIC_FEATURE_COLUMNS = []

EXPECTED_RISK_LABELS = ("Low", "Medium", "High")

# Inclusive academic/operational ranges used for raw input checks.
FEATURE_RANGES = {
    "CGPA": (0.0, 10.0),
    "Attendance_Percent": (0.0, 100.0),
    "Backlogs": (0, 20),
    "Average_Test_Score_Percent": (0.0, 100.0),
}
