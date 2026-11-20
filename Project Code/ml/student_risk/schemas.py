from __future__ import annotations

from typing import Any

import pandas as pd
from pydantic import BaseModel, ConfigDict, Field, model_validator
from typing_extensions import Self

from student_risk.config import FEATURE_COLUMNS
from student_risk.prepare import (
    categorize_assignments,
    categorize_attendance,
    categorize_average_test_score,
    categorize_backlogs,
    categorize_cgpa,
)


class StudentFeatures(BaseModel):
    """The 5 academic features used by the models (accepting numbers or category strings)."""

    model_config = ConfigDict(
        extra="ignore",
        json_schema_extra={
            "example": {
                "CGPA": None,
                "Backlogs": 2,
                "Attendance_Percent": 61.9,
                "Assignments_Percent": 20.0,
                "Average_Test_Score_Percent": 51.55,
            }
        },
    )

    CGPA: Any = Field(default=None, description="CGPA numeric value, category string, or None if Backlogs > 0")
    Backlogs: Any = Field(default=0, description="Backlogs count or category string")
    Attendance_Percent: Any = Field(default=None, description="Attendance percentage or category string")
    Assignments_Percent: Any = Field(default=None, description="Assignments percentage or category string")
    Average_Test_Score_Percent: Any = Field(default=None, description="Average test score % or category string")

    # Legacy field fallbacks for backwards compatibility
    Current_Semester_Attendance: float | None = None
    Previous_Semester_Attendance: float | None = None
    Current_Semester_Test_Score: float | None = None
    Previous_Semester_Test_Score: float | None = None
    Assignment_Completion_Percent: Any = None

    @model_validator(mode="after")
    def handle_features_rules(self) -> Self:
        raw_backlogs = self.Backlogs

        # 1. Attendance fallback & categorization
        raw_att = self.Attendance_Percent
        if raw_att is None:
            if self.Current_Semester_Attendance is not None:
                if self.Previous_Semester_Attendance is not None:
                    raw_att = (self.Current_Semester_Attendance + self.Previous_Semester_Attendance) / 2.0
                else:
                    raw_att = self.Current_Semester_Attendance
            else:
                raw_att = 70.0
        self.Attendance_Percent = categorize_attendance(raw_att)

        # 2. Backlogs categorization
        self.Backlogs = categorize_backlogs(raw_backlogs)

        # 3. CGPA categorization (missing or Backlogs > 0 -> "Medium")
        self.CGPA = categorize_cgpa(self.CGPA, raw_backlogs)

        # 4. Assignments categorization
        raw_assignment = self.Assignments_Percent
        if raw_assignment is None and self.Assignment_Completion_Percent is not None:
            raw_assignment = self.Assignment_Completion_Percent
        self.Assignments_Percent = categorize_assignments(raw_assignment)

        # 5. Average test score fallback & categorization
        raw_test = self.Average_Test_Score_Percent
        if raw_test is None:
            if self.Current_Semester_Test_Score is not None:
                if self.Previous_Semester_Test_Score is not None:
                    raw_test = (self.Current_Semester_Test_Score + self.Previous_Semester_Test_Score) / 2.0
                else:
                    raw_test = self.Current_Semester_Test_Score
            else:
                raw_test = 50.0
        self.Average_Test_Score_Percent = categorize_average_test_score(raw_test)

        return self

    def to_frame(self) -> pd.DataFrame:
        row = {
            "Attendance_Percent": self.Attendance_Percent,
            "CGPA": self.CGPA,
            "Backlogs": self.Backlogs,
            "Assignments_Percent": self.Assignments_Percent,
            "Average_Test_Score_Percent": self.Average_Test_Score_Percent,
        }
        return pd.DataFrame([row], columns=FEATURE_COLUMNS)


class ModelPrediction(BaseModel):
    model: str
    Academic_Risk: str
    probabilities: dict[str, float]


class PredictResponse(BaseModel):
    Academic_Risk: str
    probabilities: dict[str, float]
    model: str
    comparison: ModelPrediction


class HealthResponse(BaseModel):
    status: str
    primary_model: str
    comparison_model: str
    features: list[str]
    loaded: bool
