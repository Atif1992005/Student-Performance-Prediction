from __future__ import annotations

from pathlib import Path
from typing import Any

import matplotlib.pyplot as plt
import pandas as pd
import seaborn as sns
from sklearn.metrics import (
    accuracy_score,
    classification_report,
    confusion_matrix,
    f1_score,
    precision_score,
    recall_score,
)
from sklearn.pipeline import Pipeline

from student_risk.config import REPORTS_DIR
from student_risk.eda import RISK_ORDER, _save


def _metrics(y_true: pd.Series, y_pred: pd.Series) -> dict[str, Any]:
    return {
        "accuracy": float(accuracy_score(y_true, y_pred)),
        "precision_macro": float(
            precision_score(y_true, y_pred, average="macro", zero_division=0)
        ),
        "recall_macro": float(
            recall_score(y_true, y_pred, average="macro", zero_division=0)
        ),
        "f1_macro": float(f1_score(y_true, y_pred, average="macro", zero_division=0)),
        "precision_weighted": float(
            precision_score(y_true, y_pred, average="weighted", zero_division=0)
        ),
        "recall_weighted": float(
            recall_score(y_true, y_pred, average="weighted", zero_division=0)
        ),
        "f1_weighted": float(
            f1_score(y_true, y_pred, average="weighted", zero_division=0)
        ),
        "per_class": classification_report(
            y_true,
            y_pred,
            labels=RISK_ORDER,
            output_dict=True,
            zero_division=0,
        ),
    }


def plot_confusion_matrix(y_true: pd.Series, y_pred: pd.Series, model_name: str) -> Path:
    matrix = confusion_matrix(y_true, y_pred, labels=RISK_ORDER)
    fig, ax = plt.subplots(figsize=(6.5, 5.5))
    sns.heatmap(
        matrix,
        annot=True,
        fmt="d",
        cmap="Blues",
        xticklabels=RISK_ORDER,
        yticklabels=RISK_ORDER,
        ax=ax,
        cbar=False,
    )
    ax.set_title(f"Confusion Matrix — {model_name}")
    ax.set_xlabel("Predicted")
    ax.set_ylabel("Actual")
    slug = model_name.lower().replace(" ", "_")
    return _save(fig, f"stage2_confusion_{slug}.png")


def plot_model_comparison(table: pd.DataFrame) -> Path:
    metric_cols = ["accuracy", "precision_macro", "recall_macro", "f1_macro"]
    long = table.melt(
        id_vars=["model"],
        value_vars=metric_cols,
        var_name="metric",
        value_name="score",
    )
    fig, ax = plt.subplots(figsize=(9, 5))
    sns.barplot(data=long, x="metric", y="score", hue="model", ax=ax)
    ax.set_ylim(0, 1.05)
    ax.set_title("Model Comparison (test set)")
    ax.set_xlabel("")
    ax.set_ylabel("Score")
    ax.legend(title="Model")
    return _save(fig, "stage2_model_comparison.png")


def evaluate_models(
    models: dict[str, Pipeline],
    X_test: pd.DataFrame,
    y_test: pd.Series,
) -> tuple[pd.DataFrame, dict[str, Any], list[Path]]:
    rows: list[dict[str, Any]] = []
    details: dict[str, Any] = {}
    figure_paths: list[Path] = []

    for name, model in models.items():
        y_pred = model.predict(X_test)
        scores = _metrics(y_test, y_pred)
        details[name] = scores
        rows.append(
            {
                "model": name,
                "accuracy": scores["accuracy"],
                "precision_macro": scores["precision_macro"],
                "recall_macro": scores["recall_macro"],
                "f1_macro": scores["f1_macro"],
                "precision_weighted": scores["precision_weighted"],
                "recall_weighted": scores["recall_weighted"],
                "f1_weighted": scores["f1_weighted"],
            }
        )
        figure_paths.append(plot_confusion_matrix(y_test, y_pred, name))

    table = pd.DataFrame(rows).sort_values("f1_macro", ascending=False).reset_index(drop=True)
    figure_paths.append(plot_model_comparison(table))

    REPORTS_DIR.mkdir(parents=True, exist_ok=True)
    comparison_path = REPORTS_DIR / "model_comparison.csv"
    table.to_csv(comparison_path, index=False)
    return table, details, figure_paths


def format_comparison_table(table: pd.DataFrame) -> str:
    display = table.copy()
    score_cols = [col for col in display.columns if col != "model"]
    for col in score_cols:
        display[col] = display[col].map(lambda value: f"{value:.4f}")
    return display.to_string(index=False)
