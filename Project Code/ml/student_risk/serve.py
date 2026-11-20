from __future__ import annotations

import json
from pathlib import Path
from typing import Any

from joblib import dump, load
from sklearn.pipeline import Pipeline

from student_risk.config import (
    COMPARISON_MODEL_NAME,
    COMPARISON_MODEL_PATH,
    EXPECTED_RISK_LABELS,
    FEATURE_COLUMNS,
    FEATURE_RANGES,
    MODELS_DIR,
    PRIMARY_MODEL_NAME,
    PRIMARY_MODEL_PATH,
    SERVING_METADATA_PATH,
)
from student_risk.schemas import ModelPrediction, PredictResponse, StudentFeatures


def _slug(name: str) -> str:
    return name.lower().replace(" ", "_")


def _stage2_path(name: str) -> Path:
    return MODELS_DIR / f"{_slug(name)}.joblib"


def export_serving_artifacts(models: dict[str, Pipeline], extra_metadata: dict[str, Any] | None = None) -> dict[str, str]:
    """Persist the primary Logistic Regression pipeline and Random Forest comparison model."""
    if PRIMARY_MODEL_NAME not in models:
        raise KeyError(f"Primary model '{PRIMARY_MODEL_NAME}' was not trained.")
    if COMPARISON_MODEL_NAME not in models:
        raise KeyError(f"Comparison model '{COMPARISON_MODEL_NAME}' was not trained.")

    MODELS_DIR.mkdir(parents=True, exist_ok=True)
    dump(models[PRIMARY_MODEL_NAME], PRIMARY_MODEL_PATH)
    dump(models[COMPARISON_MODEL_NAME], COMPARISON_MODEL_PATH)

    primary = models[PRIMARY_MODEL_NAME]
    metadata = {
        "primary_model": PRIMARY_MODEL_NAME,
        "comparison_model": COMPARISON_MODEL_NAME,
        "features": list(FEATURE_COLUMNS),
        "feature_ranges": {k: list(v) for k, v in FEATURE_RANGES.items()},
        "classes": [str(label) for label in primary.classes_],
        "preprocessing": {
            PRIMARY_MODEL_NAME: "sklearn Pipeline with OneHotEncoder on standardized categories (Low, Medium, High)",
            COMPARISON_MODEL_NAME: "sklearn Pipeline with OneHotEncoder on standardized categories (Low, Medium, High)",
        },
        "paths": {
            "primary": str(PRIMARY_MODEL_PATH),
            "comparison": str(COMPARISON_MODEL_PATH),
        },
    }
    if extra_metadata:
        metadata.update(extra_metadata)
    SERVING_METADATA_PATH.write_text(json.dumps(metadata, indent=2), encoding="utf-8")
    return metadata["paths"]


def load_pipeline(path: Path) -> Pipeline:
    if not path.exists():
        fallback = _stage2_path(
            PRIMARY_MODEL_NAME if path == PRIMARY_MODEL_PATH else COMPARISON_MODEL_NAME
        )
        if fallback.exists():
            path = fallback
        else:
            raise FileNotFoundError(
                f"Saved model not found at {path}. Run Stage 2 or Stage 3 first."
            )
    return load(path)


class ModelBundle:
    def __init__(self) -> None:
        self.primary = load_pipeline(PRIMARY_MODEL_PATH)
        self.comparison = load_pipeline(COMPARISON_MODEL_PATH)
        self.primary_name = PRIMARY_MODEL_NAME
        self.comparison_name = COMPARISON_MODEL_NAME

    def predict(self, features: StudentFeatures) -> PredictResponse:
        frame = features.to_frame()
        primary_result = self._one(self.primary, self.primary_name, frame)
        comparison_result = self._one(self.comparison, self.comparison_name, frame)
        return PredictResponse(
            Academic_Risk=primary_result.Academic_Risk,
            probabilities=primary_result.probabilities,
            model=primary_result.model,
            comparison=comparison_result,
        )

    @staticmethod
    def _one(pipeline: Pipeline, name: str, frame) -> ModelPrediction:
        label = str(pipeline.predict(frame)[0])
        proba = pipeline.predict_proba(frame)[0]
        raw = {
            str(class_name): round(float(score), 6)
            for class_name, score in zip(pipeline.classes_, proba)
        }
        probabilities = {label: raw.get(label, 0.0) for label in EXPECTED_RISK_LABELS}
        return ModelPrediction(
            model=name,
            Academic_Risk=label,
            probabilities=probabilities,
        )
