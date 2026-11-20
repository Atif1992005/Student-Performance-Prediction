from __future__ import annotations

import json
from pathlib import Path

from joblib import dump, load

from student_risk.config import (
    COMPARISON_MODEL_NAME,
    MODELS_DIR,
    PRIMARY_MODEL_NAME,
    PROCESSED_DIR,
    REPORTS_DIR,
    SERVING_METADATA_PATH,
    TARGET_COLUMN,
)
from student_risk.eda import feature_summary, run_eda
from student_risk.evaluate import evaluate_models, format_comparison_table
from student_risk.load import load_prepared_dataset, load_raw_dataset
from student_risk.prepare import build_modeling_frame
from student_risk.serve import export_serving_artifacts
from student_risk.train import split_summary, stratified_split, train_models
from student_risk.validate import validate_dataset


def _write_json(path: Path, payload: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(payload, indent=2, default=float), encoding="utf-8")


def run_stage1(raw_path: Path | None = None) -> dict:
    """Load, validate, prepare features, and generate EDA. Does not train models."""
    raw = load_raw_dataset(raw_path)
    quality = validate_dataset(raw)
    modeling_frame = build_modeling_frame(raw)

    PROCESSED_DIR.mkdir(parents=True, exist_ok=True)
    REPORTS_DIR.mkdir(parents=True, exist_ok=True)

    processed_path = PROCESSED_DIR / "features_and_target.csv"
    modeling_frame.to_csv(processed_path, index=False)

    summary = feature_summary(modeling_frame)
    summary_path = REPORTS_DIR / "feature_summary.csv"
    summary.to_csv(summary_path)

    figure_paths = run_eda(modeling_frame)

    report = {
        "stage": 1,
        "trained_model": False,
        "data_quality": quality,
        "prepared_rows": int(len(modeling_frame)),
        "prepared_columns": list(modeling_frame.columns),
        "target_balance": modeling_frame[TARGET_COLUMN]
        .value_counts()
        .astype(int)
        .to_dict(),
        "outputs": {
            "processed_dataset": str(processed_path),
            "feature_summary": str(summary_path),
            "figures": [str(path) for path in figure_paths],
        },
    }
    _write_json(REPORTS_DIR / "stage1_report.json", report)
    return report


def run_stage2(prepared_path: Path | None = None) -> dict:
    """Train and evaluate Logistic Regression, Decision Tree, and Random Forest."""
    frame = load_prepared_dataset(prepared_path)
    X_train, X_test, y_train, y_test = stratified_split(frame)
    models = train_models(X_train, y_train)
    table, details, figure_paths = evaluate_models(models, X_test, y_test)

    MODELS_DIR.mkdir(parents=True, exist_ok=True)
    model_paths: dict[str, str] = {}
    for name, model in models.items():
        slug = name.lower().replace(" ", "_")
        path = MODELS_DIR / f"{slug}.joblib"
        dump(model, path)
        model_paths[name] = str(path)

    best_row = table.iloc[0]
    report = {
        "stage": 2,
        "split": split_summary(y_train, y_test),
        "preprocessing": {
            "Logistic Regression": "OneHotEncoder on standardized categories (Low, Medium, High) for all 5 features",
            "Decision Tree": "OneHotEncoder on standardized categories (Low, Medium, High) for all 5 features",
            "Random Forest": "OneHotEncoder on standardized categories (Low, Medium, High) for all 5 features",
            "class_weight": "balanced",
        },
        "comparison_table": table.to_dict(orient="records"),
        "metrics_detail": details,
        "best_model": {
            "name": best_row["model"],
            "f1_macro": float(best_row["f1_macro"]),
            "accuracy": float(best_row["accuracy"]),
        },
        "outputs": {
            "comparison_csv": str(REPORTS_DIR / "model_comparison.csv"),
            "models": model_paths,
            "figures": [str(path) for path in figure_paths],
        },
    }
    _write_json(REPORTS_DIR / "stage2_report.json", report)
    report["comparison_text"] = format_comparison_table(table)
    return report


def _load_or_train_serving_models() -> dict:
    primary_stage2 = MODELS_DIR / "logistic_regression.joblib"
    comparison_stage2 = MODELS_DIR / "random_forest.joblib"
    if primary_stage2.exists() and comparison_stage2.exists():
        return {
            PRIMARY_MODEL_NAME: load(primary_stage2),
            COMPARISON_MODEL_NAME: load(comparison_stage2),
        }

    frame = load_prepared_dataset()
    X_train, _X_test, y_train, _y_test = stratified_split(frame)
    trained = train_models(X_train, y_train)
    return {
        PRIMARY_MODEL_NAME: trained[PRIMARY_MODEL_NAME],
        COMPARISON_MODEL_NAME: trained[COMPARISON_MODEL_NAME],
    }


def run_stage3() -> dict:
    """Export the final Logistic Regression pipeline and Random Forest comparison model."""
    models = _load_or_train_serving_models()
    paths = export_serving_artifacts(
        models,
        extra_metadata={"stage": 3, "selection_reason": "highest macro F1 on Stage 2 test set"},
    )
    report = {
        "stage": 3,
        "primary_model": PRIMARY_MODEL_NAME,
        "comparison_model": COMPARISON_MODEL_NAME,
        "selection_reason": "Logistic Regression had the best Stage 2 macro F1.",
        "outputs": {
            "models": paths,
            "metadata": str(SERVING_METADATA_PATH),
        },
    }
    _write_json(REPORTS_DIR / "stage3_report.json", report)
    return report
