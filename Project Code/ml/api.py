from __future__ import annotations

from contextlib import asynccontextmanager

from fastapi import FastAPI, HTTPException

from student_risk.config import COMPARISON_MODEL_NAME, FEATURE_COLUMNS, PRIMARY_MODEL_NAME
from student_risk.schemas import HealthResponse, PredictResponse, StudentFeatures
from student_risk.serve import ModelBundle

bundle: ModelBundle | None = None


@asynccontextmanager
async def lifespan(_app: FastAPI):
    global bundle
    bundle = ModelBundle()
    yield
    bundle = None


app = FastAPI(
    title="Student Academic Risk API",
    description="Stage 3 FastAPI service for the Logistic Regression academic-risk model.",
    version="0.3.0",
    lifespan=lifespan,
)


@app.get("/health", response_model=HealthResponse)
def health() -> HealthResponse:
    return HealthResponse(
        status="ok",
        primary_model=PRIMARY_MODEL_NAME,
        comparison_model=COMPARISON_MODEL_NAME,
        features=list(FEATURE_COLUMNS),
        loaded=bundle is not None,
    )


@app.post("/predict", response_model=PredictResponse)
def predict(payload: StudentFeatures) -> PredictResponse:
    if bundle is None:
        raise HTTPException(status_code=503, detail="Models are not loaded.")
    return bundle.predict(payload)
