from __future__ import annotations

from typing import Any

import pandas as pd

from student_risk.config import (
    ASSIGNMENT_CATEGORIES,
    EXPECTED_RISK_LABELS,
    FEATURE_COLUMNS,
    FEATURE_RANGES,
    NUMERIC_FEATURE_COLUMNS,
    TARGET_COLUMN,
)


def _range_violations(series: pd.Series, low: float, high: float) -> int:
    numeric = pd.to_numeric(series, errors="coerce")
    return int(((numeric < low) | (numeric > high)).sum())


def validate_dataset(df: pd.DataFrame) -> dict[str, Any]:
    """Inspect schema, missing values, duplicates, labels, and numeric ranges."""
    expected_columns = list(FEATURE_COLUMNS) + [TARGET_COLUMN]

    missing_columns = [col for col in expected_columns if col not in df.columns]
    extra_columns = [col for col in df.columns if col not in expected_columns]

    missing_counts = df.isna().sum().astype(int).to_dict()
    duplicate_rows = int(df.duplicated().sum())

    risk_counts = (
        df[TARGET_COLUMN].value_counts(dropna=False).to_dict()
        if TARGET_COLUMN in df.columns
        else {}
    )
    unexpected_labels = sorted(
        set(df[TARGET_COLUMN].dropna().astype(str).unique())
        - set(EXPECTED_RISK_LABELS)
    ) if TARGET_COLUMN in df.columns else []

    unexpected_assignments = sorted(
        set(df["Assignments_Percent"].dropna().astype(str).unique())
        - set(ASSIGNMENT_CATEGORIES)
    ) if "Assignments_Percent" in df.columns else []

    range_report: dict[str, Any] = {}
    total_out_of_range = 0
    for column, (low, high) in FEATURE_RANGES.items():
        if column not in df.columns:
            range_report[column] = {"status": "missing"}
            continue
        n_invalid = _range_violations(df[column], low, high)
        numeric = pd.to_numeric(df[column], errors="coerce")
        total_out_of_range += n_invalid
        range_report[column] = {
            "expected": [low, high],
            "observed_min": None if numeric.dropna().empty else float(numeric.min()),
            "observed_max": None if numeric.dropna().empty else float(numeric.max()),
            "out_of_range_count": n_invalid,
        }

    issues: list[str] = []
    if missing_columns:
        issues.append(f"Missing expected columns: {missing_columns}")
    if duplicate_rows:
        issues.append(f"Found {duplicate_rows} duplicate rows")

    allowed_missing_cols = {"CGPA"}
    disallowed_missing = {
        col: count
        for col, count in missing_counts.items()
        if col not in allowed_missing_cols
        and col in expected_columns
        and count > 0
    }
    if disallowed_missing:
        issues.append(f"Missing values present in non-exempt columns: {disallowed_missing}")

    if "Backlogs" in df.columns and "CGPA" in df.columns:
        zero_backlogs_missing_cgpa = int(
            ((pd.to_numeric(df["Backlogs"], errors="coerce") == 0) & df["CGPA"].isna()).sum()
        )
        if zero_backlogs_missing_cgpa > 0:
            issues.append(f"Found {zero_backlogs_missing_cgpa} rows with 0 backlogs but missing CGPA")

    if unexpected_labels:
        issues.append(f"Unexpected Academic_Risk labels: {unexpected_labels}")
    if unexpected_assignments:
        issues.append(f"Unexpected Assignments_Percent categories: {unexpected_assignments}")
    if total_out_of_range:
        issues.append(f"{total_out_of_range} values fall outside expected ranges")

    return {
        "passed": len(issues) == 0,
        "n_rows": int(len(df)),
        "n_columns": int(len(df.columns)),
        "columns": list(df.columns),
        "missing_columns": missing_columns,
        "extra_columns": extra_columns,
        "missing_counts": missing_counts,
        "duplicate_rows": duplicate_rows,
        "risk_counts": risk_counts,
        "unexpected_labels": unexpected_labels,
        "ranges": range_report,
        "issues": issues,
    }
