import os
from pathlib import Path
import matplotlib.pyplot as plt
import numpy as np

# Ensure publication/presentation styling
plt.rcParams['font.sans-serif'] = 'DejaVu Sans'
plt.rcParams['font.family'] = 'sans-serif'

script_dir = Path(__file__).resolve().parent
project_root = script_dir.parent

# Models and placeholder benchmark metrics for demonstration:
# Logistic Regression 96%, Random Forest 94%, Decision Tree 92%
models = [
    'Logistic Regression\n(Selected Production Model)',
    'Random Forest\n(Benchmark)',
    'Decision Tree\n(Baseline)'
]

accuracy = [96.00, 94.00, 92.00]
macro_f1 = [95.80, 93.80, 91.80]

x = np.arange(len(models))
width = 0.32

fig, ax = plt.subplots(figsize=(9.5, 6.2), dpi=300)

bars_acc = ax.bar(x - width/2, accuracy, width, label='Accuracy', color='#2563eb', edgecolor='#1e40af', linewidth=1.2, zorder=3)
bars_f1 = ax.bar(x + width/2, macro_f1, width, label='Macro F1-Score', color='#10b981', edgecolor='#047857', linewidth=1.2, zorder=3)

# Highlight Logistic Regression bars as the selected production model
bars_acc[0].set_color('#1d4ed8')
bars_acc[0].set_edgecolor('#172554')
bars_acc[0].set_linewidth(2.0)

bars_f1[0].set_color('#059669')
bars_f1[0].set_edgecolor('#064e3b')
bars_f1[0].set_linewidth(2.0)

# Styling and Grid
ax.set_ylabel('Score (%)', fontsize=12, fontweight='bold', labelpad=10, color='#1f2937')
ax.set_title('Model Performance Comparison (Test Set Evaluation)', fontsize=15, fontweight='bold', pad=24, color='#111827')
ax.set_xticks(x)
ax.set_xticklabels(models, fontsize=11, fontweight='bold', color='#1f2937')
ax.set_ylim(80, 106)
ax.set_yticks(np.arange(80, 106, 5))
ax.yaxis.grid(True, linestyle='--', alpha=0.5, color='#cbd5e1', zorder=0)
ax.set_axisbelow(True)

# Highlight badge over Logistic Regression
ax.annotate(
    'Selected Production Model (96%)',
    xy=(0, accuracy[0] + 0.5),
    xytext=(0, 102.5),
    ha='center',
    va='center',
    fontsize=10.5,
    fontweight='bold',
    color='#1e3a8a',
    bbox=dict(boxstyle='round,pad=0.45', facecolor='#dbeafe', edgecolor='#2563eb', linewidth=1.6),
    arrowprops=dict(arrowstyle='->', connectionstyle='arc3,rad=0', color='#1d4ed8', lw=1.6),
    zorder=5
)

# Add exact value labels on top of bars
def autolabel(bars, offset_y=0.7):
    for bar in bars:
        height = bar.get_height()
        ax.annotate(
            f'{height:.2f}%',
            xy=(bar.get_x() + bar.get_width() / 2, height),
            xytext=(0, offset_y),
            textcoords="offset points",
            ha='center',
            va='bottom',
            fontsize=10.5,
            fontweight='bold',
            color='#1f2937',
            zorder=4
        )

autolabel(bars_acc)
autolabel(bars_f1)

# Subtle background box around Logistic Regression column to visually emphasize selection
ax.axvspan(-0.45, 0.45, facecolor='#eff6ff', alpha=0.6, zorder=1, linestyle=':', edgecolor='#93c5fd', linewidth=1.4)

# Legend
legend = ax.legend(loc='lower right', frameon=True, facecolor='#ffffff', edgecolor='#cbd5e1', fontsize=10.5, framealpha=0.95)
legend.get_frame().set_linewidth(1.0)

# Note on model selection criteria
fig.text(
    0.5, 0.02,
    'Note: Placeholder benchmarks for demonstration & viva presentation. Logistic Regression (96%) selected for production deployment.',
    ha='center',
    fontsize=9.5,
    fontstyle='italic',
    color='#4b5563',
    bbox=dict(boxstyle='square,pad=0.4', facecolor='#f8fafc', edgecolor='#e2e8f0', linewidth=1)
)

plt.tight_layout(rect=[0, 0.05, 1, 0.96])

output_dir = script_dir / 'reports' / 'figures'
os.makedirs(output_dir, exist_ok=True)

png_path = output_dir / 'model_performance_comparison.png'
pdf_path = output_dir / 'model_performance_comparison.pdf'
ml_png = script_dir / 'model_performance_comparison.png'
ml_pdf = script_dir / 'model_performance_comparison.pdf'
root_png = project_root / 'model_performance_comparison.png'
root_pdf = project_root / 'model_performance_comparison.pdf'
artifact_dir = Path(r"C:\Users\atif1\.gemini\antigravity\brain\4fc2d05e-e478-4d3b-866b-0d3ac0db46be")

# Save outputs to all distribution locations
for p in [png_path, ml_png, root_png]:
    plt.savefig(p, dpi=300)
for p in [pdf_path, ml_pdf, root_pdf]:
    plt.savefig(p)

if artifact_dir.exists():
    plt.savefig(artifact_dir / 'model_performance_comparison.png', dpi=300)
    plt.savefig(artifact_dir / 'model_performance_comparison.pdf')

plt.close()

print('Generated files successfully:')
print('  -', png_path)
print('  -', pdf_path)
print('  -', ml_png)
print('  -', ml_pdf)
print('  -', root_png)
print('  -', root_pdf)
