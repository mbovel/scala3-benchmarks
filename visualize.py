#!/usr/bin/env python3
"""Visualize benchmark results as box plots using Plotly Express."""

import json
from pathlib import Path

import pandas as pd
import plotly.express as px

RESULTS_DIR = Path("results")
BASELINE_VERSION = "3.3.4"
LAST_N_MEASUREMENTS = 20  # Keep only last 20 measurements per run (after warmup)


def load_all_data() -> pd.DataFrame:
    """Load all benchmark data from the results directory."""
    rows = []

    for version_dir in sorted(RESULTS_DIR.rglob("*")):
        if not version_dir.is_dir():
            continue
        json_files = list(version_dir.glob("*.json"))
        if not json_files:
            continue

        version = version_dir.name

        for json_file in json_files:
            run_name = json_file.stem
            with open(json_file) as f:
                benchmarks = json.load(f)

            for bench in benchmarks:
                benchmark_name = bench["benchmark"].replace(
                    "bench.CompilationBenchmarks.", ""
                )
                raw_data = bench["primaryMetric"]["rawData"]
                # rawData is a list of lists (one per fork), flatten and take last N
                measurements = [m for fork_data in raw_data for m in fork_data]
                last_measurements = measurements[-LAST_N_MEASUREMENTS:]

                for i, value in enumerate(last_measurements):
                    rows.append(
                        {
                            "version": version,
                            "benchmark": benchmark_name,
                            "run": run_name,
                            "measurement_idx": i,
                            "time_ms": value,
                        }
                    )

    return pd.DataFrame(rows)


def compute_relative_speeds(df: pd.DataFrame) -> pd.DataFrame:
    """Compute speed relative to the baseline version (higher = faster)."""
    # Compute median time for each benchmark in the baseline version
    baseline_medians = (
        df[df["version"] == BASELINE_VERSION]
        .groupby("benchmark")["time_ms"]
        .median()
        .to_dict()
    )

    # Compute relative speed: baseline_time / current_time
    # Values > 1 mean faster than baseline, < 1 mean slower
    df["relative_speed"] = df.apply(
        lambda row: baseline_medians.get(row["benchmark"], 1) / row["time_ms"], axis=1
    )

    return df


def main():
    df = load_all_data()
    print(f"Loaded {len(df)} data points")
    print(f"Versions: {sorted(df['version'].unique())}")
    print(f"Benchmarks: {sorted(df['benchmark'].unique())}")

    df = compute_relative_speeds(df)

    # Sort versions for consistent ordering
    version_order = ["3.3.4", "3.7.4", "3.8.0-RC2", "3.8.1-RC1-bin-SNAPSHOT"]
    df["version"] = pd.Categorical(df["version"], categories=version_order, ordered=True)

    fig = px.box(
        df,
        x="benchmark",
        y="relative_speed",
        color="version",
        title="Benchmark Performance Relative to 3.3.4 (higher = faster)",
        labels={
            "benchmark": "Benchmark",
            "relative_speed": "Relative Speed (vs 3.3.4)",
            "version": "Version",
        },
        category_orders={"version": version_order},
    )

    fig.update_layout(
        xaxis_tickangle=-45,
        height=900,
        legend=dict(orientation="h", yanchor="bottom", y=1.02, xanchor="right", x=1),
    )

    # Add a horizontal line at y=1 (baseline)
    fig.add_hline(y=1, line_dash="dash", line_color="gray", opacity=0.5)

    output_file = "benchmark_results.html"
    fig.write_html(output_file, include_plotlyjs="cdn")
    print(f"Output written to {output_file}")


if __name__ == "__main__":
    main()
