from __future__ import annotations

import re
from typing import Any

import numpy as np
import pandas as pd

from student_risk.config import (
    FEATURE_CATEGORY_LEVELS,
    FEATURE_COLUMNS,
    TARGET_COLUMN,
)


def categorize_attendance(value: Any) -> str:
    """Attendance: Low ≥75%, Medium ≥60% and <75%, High <60%."""
    if value is None or pd.isna(value):
        return "Medium"
    text = str(value).strip()
    if text in FEATURE_CATEGORY_LEVELS:
        return text
    try:
        val = float(text.replace("%", "").strip())
        if val >= 75.0:
            return "Low"
        elif val >= 60.0:
            return "Medium"
        else:
            return "High"
    except (ValueError, TypeError):
        return "Medium"


def categorize_cgpa(value: Any, backlogs: Any = 0) -> str:
    """CGPA: Low ≥7.5, Medium ≥6 and <7.5, High <6; missing = Medium."""
    try:
        b_num = int(float(str(backlogs).strip())) if backlogs is not None else 0
        if b_num > 0:
            return "Medium"
    except (ValueError, TypeError):
        pass

    if value is None or pd.isna(value):
        return "Medium"

    text = str(value).strip()
    if text in FEATURE_CATEGORY_LEVELS:
        return text
    if text.lower() in {"n/a", "na", "none", "null", "nan", ""}:
        return "Medium"

    try:
        val = float(text)
        if val >= 7.5:
            return "Low"
        elif val >= 6.0:
            return "Medium"
        else:
            return "High"
    except (ValueError, TypeError):
        return "Medium"


def categorize_backlogs(value: Any) -> str:
    """Backlogs: Low = 0, Medium = 1–2, High ≥3."""
    if value is None or pd.isna(value):
        return "Low"
    text = str(value).strip()
    if text in FEATURE_CATEGORY_LEVELS:
        return text
    try:
        val = int(float(text))
        if val == 0:
            return "Low"
        elif val in (1, 2):
            return "Medium"
        else:
            return "High"
    except (ValueError, TypeError):
        return "Low"


def categorize_assignments(value: Any) -> str:
    """Assignments: Low ≥60%, Medium ≥30% and <60%, High <30%."""
    if value is None or pd.isna(value):
        return "High"
    text = str(value).strip().replace(">=", "≥")
    if text in FEATURE_CATEGORY_LEVELS:
        return text

    # Handle numeric input first (e.g. 50, "50", "50%")
    try:
        val = float(text.replace("%", "").strip())
        if val >= 60.0:
            return "Low"
        elif val >= 30.0:
            return "Medium"
        else:
            return "High"
    except (ValueError, TypeError):
        pass

    # Heuristic string matching for range texts like "≥ 30% and < 60%"
    if "30" in text and "60" in text:
        return "Medium"
    if "60" in text:
        return "Low"
    if "30" in text or "<" in text:
        return "High"

    return "High"


# Legacy alias for backward compatibility
assignment_to_category = categorize_assignments


def categorize_average_test_score(value: Any) -> str:
    """Average Test Score: Low ≥60%, Medium ≥40% and <60%, High <40%."""
    if value is None or pd.isna(value):
        return "Medium"
    text = str(value).strip()
    if text in FEATURE_CATEGORY_LEVELS:
        return text
    try:
        val = float(text.replace("%", "").strip())
        if val >= 60.0:
            return "Low"
        elif val >= 40.0:
            return "Medium"
        else:
            return "High"
    except (ValueError, TypeError):
        return "Medium"


def prepare_features_and_target(df: pd.DataFrame) -> tuple[pd.DataFrame, pd.Series]:
    """Return model-ready X (5 categorized features) and y (Academic_Risk labels)."""
    prepared = df.copy()

    # Map Assignments_Percent if present, or adapt from legacy assignment columns
    if "Assignments_Percent" in prepared.columns:
        pass
    elif "Assignment_Completion_Percent" in prepared.columns:
        prepared["Assignments_Percent"] = prepared["Assignment_Completion_Percent"]
    elif "Current_Semester_Assignment_Completion" in prepared.columns:
        prepared["Assignments_Percent"] = prepared["Current_Semester_Assignment_Completion"]
    else:
        raise KeyError("Expected 'Assignments_Percent' or legacy assignment column in dataset.")

    # Map attendance if legacy
    if "Attendance_Percent" not in prepared.columns and "Current_Semester_Attendance" in prepared.columns:
        if "Previous_Semester_Attendance" in prepared.columns:
            prepared["Attendance_Percent"] = (
                pd.to_numeric(prepared["Current_Semester_Attendance"], errors="coerce")
                + pd.to_numeric(prepared["Previous_Semester_Attendance"], errors="coerce")
            ) / 2.0
        else:
            prepared["Attendance_Percent"] = pd.to_numeric(prepared["Current_Semester_Attendance"], errors="coerce")

    # Map test scores if legacy
    if "Average_Test_Score_Percent" not in prepared.columns and "Current_Semester_Test_Score" in prepared.columns:
        if "Previous_Semester_Test_Score" in prepared.columns:
            prepared["Average_Test_Score_Percent"] = (
                pd.to_numeric(prepared["Current_Semester_Test_Score"], errors="coerce")
                + pd.to_numeric(prepared["Previous_Semester_Test_Score"], errors="coerce")
            ) / 2.0
        else:
            prepared["Average_Test_Score_Percent"] = pd.to_numeric(prepared["Current_Semester_Test_Score"], errors="coerce")

    missing_features = [col for col in FEATURE_COLUMNS if col not in prepared.columns]
    if missing_features:
        raise KeyError(f"Missing feature columns: {missing_features}")
    if TARGET_COLUMN not in prepared.columns:
        raise KeyError(f"Missing target column: {TARGET_COLUMN}")

    # Apply categorization rules
    X = pd.DataFrame(index=prepared.index)
    X["Attendance_Percent"] = prepared["Attendance_Percent"].map(categorize_attendance)
    X["CGPA"] = prepared.apply(lambda r: categorize_cgpa(r.get("CGPA"), r.get("Backlogs")), axis=1)
    X["Backlogs"] = prepared["Backlogs"].map(categorize_backlogs)
    X["Assignments_Percent"] = prepared["Assignments_Percent"].map(categorize_assignments)
    X["Average_Test_Score_Percent"] = prepared["Average_Test_Score_Percent"].map(categorize_average_test_score)

    X = X[FEATURE_COLUMNS].copy()
    y = prepared[TARGET_COLUMN].astype("string").str.strip()
    return X, y


def build_modeling_frame(df: pd.DataFrame) -> pd.DataFrame:
    """Single table with prepared features plus the target label."""
    X, y = prepare_features_and_target(df)
    frame = X.copy()
    frame[TARGET_COLUMN] = y
    return frame
