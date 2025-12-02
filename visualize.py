#!/usr/bin/env python3
"""Visualize benchmark results as box plots using Plotly Express."""

import argparse
import json
from pathlib import Path

import pandas as pd
import plotly.express as px

RESULTS_DIR = Path("results")
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
                # rawData is a list of lists (one per fork)
                # Take last N measurements from each fork (JVM warms up separately per fork)
                last_measurements = [
                    m for fork_data in raw_data for m in fork_data[-LAST_N_MEASUREMENTS:]
                ]

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


def compute_relative_speeds(df: pd.DataFrame, baseline_version: str) -> pd.DataFrame:
    """Compute speed relative to the baseline version (higher = faster)."""
    # Compute median time for each benchmark in the baseline version
    baseline_medians = (
        df[df["version"] == baseline_version]
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
    parser = argparse.ArgumentParser(
        description="Visualize benchmark results as box plots."
    )
    parser.add_argument(
        "--versions",
        nargs="+",
        help="Ordered list of versions to compare. The first version is used as the baseline.",
    )
    parser.add_argument(
        "-o", "--output",
        default="benchmark_results.html",
        help="Output HTML file (default: benchmark_results.html)",
    )
    parser.add_argument(
        "--y-zero",
        action="store_true",
        help="Start y-axis at 0",
    )
    args = parser.parse_args()

    versions = args.versions
    baseline_version = versions[0]

    df = load_all_data()

    # Filter to only requested versions
    df = df[df["version"].isin(versions)]

    print(f"Loaded {len(df)} data points")
    print(f"Versions: {versions} (baseline: {baseline_version})")
    print(f"Benchmarks: {sorted(df['benchmark'].unique())}")

    df = compute_relative_speeds(df, baseline_version)

    # Use the provided version order
    df["version"] = pd.Categorical(df["version"], categories=versions, ordered=True)

    fig = px.box(
        df,
        x="benchmark",
        y="relative_speed",
        color="version",
        title=f"Benchmark Performance Relative to {baseline_version} (higher = faster)",
        labels={
            "benchmark": "Benchmark",
            "relative_speed": f"Relative Speed (vs {baseline_version})",
            "version": "Version",
        },
        category_orders={"version": versions},
    )

    layout_opts = dict(
        xaxis_tickangle=-45,
        height=900,
        legend=dict(orientation="h", yanchor="bottom", y=1.02, xanchor="right", x=1),
    )
    if args.y_zero:
        layout_opts["yaxis_rangemode"] = "tozero"
    fig.update_layout(**layout_opts)

    # Add a horizontal line at y=1 (baseline)
    fig.add_hline(y=1, line_dash="dash", line_color="gray", opacity=0.5)

    fig.write_html(args.output, include_plotlyjs="cdn")
    print(f"Output written to {args.output}")


if __name__ == "__main__":
    main()
