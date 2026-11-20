from __future__ import annotations

import sys
from pathlib import Path

ML_ROOT = Path(__file__).resolve().parent
if str(ML_ROOT) not in sys.path:
    sys.path.insert(0, str(ML_ROOT))

from student_risk.pipeline import run_stage3


def main() -> None:
    report = run_stage3()
    print("Student Academic Risk - Stage 3 (serving artifacts)")
    print(f"Primary model: {report['primary_model']}")
    print(f"Comparison model: {report['comparison_model']}")
    print("Saved pipelines:")
    for key, path in report["outputs"]["models"].items():
        print(f"  - {key}: {path}")
    print("Metadata:", report["outputs"]["metadata"])
    print()
    print("Start the API with:")
    print("  python run_api.py")


if __name__ == "__main__":
    main()
