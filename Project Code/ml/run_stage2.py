from __future__ import annotations

import sys
from pathlib import Path

ML_ROOT = Path(__file__).resolve().parent
if str(ML_ROOT) not in sys.path:
    sys.path.insert(0, str(ML_ROOT))

from student_risk.pipeline import run_stage2


def main() -> None:
    report = run_stage2()
    split = report["split"]
    print("Student Academic Risk - Stage 2 (model training)")
    print(
        f"Stratified split: {split['train_rows']:,} train / "
        f"{split['test_rows']:,} test ({1 - split['test_size']:.0%}/{split['test_size']:.0%})"
    )
    print("Train class counts:", split["train_class_counts"])
    print("Test class counts:", split["test_class_counts"])
    print()
    print("Model comparison (sorted by macro F1):")
    print(report["comparison_text"])
    print()
    best = report["best_model"]
    print(
        f"Best model: {best['name']} "
        f"(accuracy={best['accuracy']:.4f}, macro F1={best['f1_macro']:.4f})"
    )
    print("Comparison CSV:", report["outputs"]["comparison_csv"])
    print("Saved models:")
    for name, path in report["outputs"]["models"].items():
        print(f"  - {name}: {path}")
    print("Figures:")
    for path in report["outputs"]["figures"]:
        print(f"  - {path}")
    print("Full report:", ML_ROOT / "reports" / "stage2_report.json")


if __name__ == "__main__":
    main()
