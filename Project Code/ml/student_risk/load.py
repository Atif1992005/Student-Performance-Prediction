from pathlib import Path

import pandas as pd

from student_risk.config import FEATURE_COLUMNS, PREPARED_DATA_PATH, RAW_DATA_PATH, TARGET_COLUMN
from student_risk.prepare import build_modeling_frame


def load_raw_dataset(path: Path | None = None) -> pd.DataFrame:
    """Load the student academic risk CSV as a DataFrame."""
    csv_path = path or RAW_DATA_PATH
    if not csv_path.exists():
        raise FileNotFoundError(f"Dataset not found: {csv_path}")
    return pd.read_csv(csv_path)


def load_prepared_dataset(path: Path | None = None) -> pd.DataFrame:
    """Load Stage 1 features + target, rebuilding from raw data if needed."""
    csv_path = path or PREPARED_DATA_PATH
    if csv_path.exists():
        frame = pd.read_csv(csv_path)
    else:
        frame = build_modeling_frame(load_raw_dataset())
        csv_path.parent.mkdir(parents=True, exist_ok=True)
        frame.to_csv(csv_path, index=False)

    missing = [col for col in FEATURE_COLUMNS + [TARGET_COLUMN] if col not in frame.columns]
    if missing:
        raise KeyError(f"Prepared dataset is missing columns: {missing}")
    return frame[FEATURE_COLUMNS + [TARGET_COLUMN]]
