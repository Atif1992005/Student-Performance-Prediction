from __future__ import annotations

import json
import sys
from pathlib import Path

ML_ROOT = Path(__file__).resolve().parent
if str(ML_ROOT) not in sys.path:
    sys.path.insert(0, str(ML_ROOT))

from student_risk.pipeline import run_stage1


def main() -> None:
    report = run_stage1()
    quality = report["data_quality"]
    print("Student Academic Risk - Stage 1 (inspection, prep, EDA)")
    print(f"Rows: {quality['n_rows']:,} | Columns: {quality['n_columns']}")
    print(f"Duplicates: {quality['duplicate_rows']}")
    print(f"Validation passed: {quality['passed']}")
    if quality["issues"]:
        print("Issues:")
        for issue in quality["issues"]:
            print(f"  - {issue}")
    else:
        print("No missing values, duplicates, or range violations.")
    print("Target balance:", report["target_balance"])
    print("Prepared dataset:", report["outputs"]["processed_dataset"])
    print("Figures:")
    for path in report["outputs"]["figures"]:
        print(f"  - {path}")
    print("Full report:", ML_ROOT / "reports" / "stage1_report.json")
    print(json.dumps({"passed": quality["passed"], "trained_model": False}, indent=2))


if __name__ == "__main__":
    main()
