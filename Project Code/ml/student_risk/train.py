from __future__ import annotations

from typing import Any

import pandas as pd
from sklearn.compose import ColumnTransformer
from sklearn.ensemble import RandomForestClassifier
from sklearn.linear_model import LogisticRegression
from sklearn.model_selection import train_test_split
from sklearn.pipeline import Pipeline
from sklearn.preprocessing import OneHotEncoder
from sklearn.tree import DecisionTreeClassifier

from student_risk.config import (
    FEATURE_CATEGORY_LEVELS,
    FEATURE_COLUMNS,
    RANDOM_STATE,
    TARGET_COLUMN,
    TEST_SIZE,
)
from student_risk.eda import RISK_ORDER


def stratified_split(
    frame: pd.DataFrame,
) -> tuple[pd.DataFrame, pd.DataFrame, pd.Series, pd.Series]:
    """Create an 80/20 stratified train-test split on Academic_Risk."""
    X = frame[FEATURE_COLUMNS]
    y = frame[TARGET_COLUMN].astype(str).str.strip()
    return train_test_split(
        X,
        y,
        test_size=TEST_SIZE,
        random_state=RANDOM_STATE,
        stratify=y,
    )


def _build_preprocessor() -> ColumnTransformer:
    cat_transformer = OneHotEncoder(
        categories=[FEATURE_CATEGORY_LEVELS for _ in FEATURE_COLUMNS],
        handle_unknown="ignore",
        sparse_output=False,
    )

    return ColumnTransformer(
        transformers=[
            ("cat", cat_transformer, FEATURE_COLUMNS),
        ],
        remainder="drop",
    )


def build_model_zoo() -> dict[str, Pipeline]:
    """All models use one-hot encoding on the standardized categorical features."""
    return {
        "Logistic Regression": Pipeline(
            [
                ("preprocess", _build_preprocessor()),
                (
                    "model",
                    LogisticRegression(
                        max_iter=2000,
                        solver="lbfgs",
                        class_weight="balanced",
                        random_state=RANDOM_STATE,
                    ),
                ),
            ]
        ),
        "Decision Tree": Pipeline(
            [
                ("preprocess", _build_preprocessor()),
                (
                    "model",
                    DecisionTreeClassifier(
                        random_state=RANDOM_STATE,
                        class_weight="balanced",
                    ),
                ),
            ]
        ),
        "Random Forest": Pipeline(
            [
                ("preprocess", _build_preprocessor()),
                (
                    "model",
                    RandomForestClassifier(
                        n_estimators=200,
                        random_state=RANDOM_STATE,
                        class_weight="balanced",
                        n_jobs=-1,
                    ),
                ),
            ]
        ),
    }


def train_models(
    X_train: pd.DataFrame,
    y_train: pd.Series,
) -> dict[str, Pipeline]:
    fitted: dict[str, Pipeline] = {}
    for name, pipeline in build_model_zoo().items():
        fitted[name] = pipeline.fit(X_train, y_train)
    return fitted


def split_summary(y_train: pd.Series, y_test: pd.Series) -> dict[str, Any]:
    return {
        "train_rows": int(len(y_train)),
        "test_rows": int(len(y_test)),
        "test_size": float(TEST_SIZE),
        "train_class_counts": y_train.value_counts().reindex(RISK_ORDER).astype(int).to_dict(),
        "test_class_counts": y_test.value_counts().reindex(RISK_ORDER).astype(int).to_dict(),
    }
