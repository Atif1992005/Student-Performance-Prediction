from __future__ import annotations

from pathlib import Path

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd
import seaborn as sns

from student_risk.config import (
    FEATURE_CATEGORY_LEVELS,
    FEATURE_COLUMNS,
    FIGURES_DIR,
    TARGET_COLUMN,
)

RISK_ORDER = ["Low", "Medium", "High"]
RISK_PALETTE = {"Low": "#2E7D32", "Medium": "#F9A825", "High": "#C62828"}


def _save(fig: plt.Figure, name: str) -> Path:
    FIGURES_DIR.mkdir(parents=True, exist_ok=True)
    path = FIGURES_DIR / name
    fig.tight_layout()
    fig.savefig(path, dpi=150, bbox_inches="tight")
    plt.close(fig)
    return path


def plot_risk_distribution(df: pd.DataFrame) -> Path:
    counts = df[TARGET_COLUMN].value_counts().reindex(RISK_ORDER)
    fig, ax = plt.subplots(figsize=(7, 4.5))
    bars = ax.bar(counts.index, counts.values, color=[RISK_PALETTE.get(r, "#6b7280") for r in counts.index])
    ax.set_title("Academic Risk Class Distribution")
    ax.set_xlabel("Academic Risk")
    ax.set_ylabel("Number of students")
    total = counts.sum()
    for bar, value in zip(bars, counts.values):
        ax.text(
            bar.get_x() + bar.get_width() / 2,
            bar.get_height(),
            f"{value:,}\n({value / total:.1%})",
            ha="center",
            va="bottom",
            fontsize=9,
        )
    ax.set_ylim(0, counts.max() * 1.18)
    return _save(fig, "01_risk_distribution.png")


def plot_feature_distributions(df: pd.DataFrame) -> Path:
    fig, axes = plt.subplots(2, 3, figsize=(14, 8))
    axes = axes.ravel()

    for idx, col in enumerate(FEATURE_COLUMNS):
        counts = df[col].value_counts().reindex(FEATURE_CATEGORY_LEVELS).fillna(0)
        axes[idx].bar(counts.index, counts.values, color="#1e40af")
        axes[idx].set_title(col.replace("_", " "), fontsize=10)
        axes[idx].set_xlabel("")
        axes[idx].set_ylabel("Count")

    # Hide 6th unused axis
    axes[5].axis("off")
    fig.suptitle("Categorical Feature Distributions (Low / Medium / High)", fontsize=14, y=1.01)
    return _save(fig, "02_feature_histograms.png")


def plot_features_by_risk(df: pd.DataFrame) -> Path:
    fig, axes = plt.subplots(2, 3, figsize=(15, 9))
    axes = axes.ravel()

    for idx, col in enumerate(FEATURE_COLUMNS):
        sns.countplot(
            data=df,
            x=col,
            order=FEATURE_CATEGORY_LEVELS,
            hue=TARGET_COLUMN,
            hue_order=RISK_ORDER,
            palette=RISK_PALETTE,
            ax=axes[idx],
        )
        axes[idx].set_title(f"{col.replace('_', ' ')} by Risk", fontsize=10)
        axes[idx].set_xlabel("")
        axes[idx].set_ylabel("Count")
        if idx == 0:
            axes[idx].legend(title="Academic Risk")
        else:
            axes[idx].get_legend().remove()

    axes[5].axis("off")
    fig.suptitle("Feature Risk Categories vs Overall Academic Risk", fontsize=14, y=1.01)
    return _save(fig, "03_features_by_risk.png")


def plot_correlation_heatmap(df: pd.DataFrame) -> Path:
    level_map = {"Low": 0, "Medium": 1, "High": 2}
    num_df = df[FEATURE_COLUMNS].copy()
    for col in FEATURE_COLUMNS:
        num_df[col] = num_df[col].map(level_map)

    corr = num_df.corr()
    fig, ax = plt.subplots(figsize=(8, 6.5))
    sns.heatmap(
        corr,
        annot=True,
        fmt=".2f",
        cmap="RdBu_r",
        center=0,
        square=True,
        ax=ax,
        annot_kws={"size": 10},
    )
    ax.set_title("Feature Ordinal Correlation Heatmap (Low=0, Medium=1, High=2)")
    return _save(fig, "04_correlation_heatmap.png")


def feature_summary(df: pd.DataFrame) -> pd.DataFrame:
    summary_rows = []
    for col in FEATURE_COLUMNS:
        vc = df[col].value_counts().to_dict()
        summary_rows.append({
            "feature": col,
            "Low": vc.get("Low", 0),
            "Medium": vc.get("Medium", 0),
            "High": vc.get("High", 0),
        })
    return pd.DataFrame(summary_rows).set_index("feature")


def run_eda(df: pd.DataFrame) -> list[Path]:
    sns.set_theme(style="whitegrid")
    paths = [
        plot_risk_distribution(df),
        plot_feature_distributions(df),
        plot_features_by_risk(df),
        plot_correlation_heatmap(df),
    ]
    return paths
