#!/bin/bash
set -euo pipefail

# Default values
VERSIONS=()
JVM="temurin:21"
RUNS=1
MACHINE=$(hostname)

# Parse arguments
while [[ $# -gt 0 ]]; do
  case $1 in
    --versions)
      shift
      while [[ $# -gt 0 && ! $1 =~ ^-- ]]; do
        VERSIONS+=("$1")
        shift
      done
      ;;
    --jvm)
      JVM="$2"
      shift 2
      ;;
    --runs)
      RUNS="$2"
      shift 2
      ;;
    --machine)
      MACHINE="$2"
      shift 2
      ;;
    *)
      echo "Unknown option: $1"
      echo "Usage: $0 --versions <v1> <v2> ... --jvm <jvm> --runs <n> [--machine <name>]"
      echo "Example: $0 --versions 3.3.4 3.7.4 3.8.0-RC2 --jvm temurin:21 --runs 3"
      exit 1
      ;;
  esac
done

# Validate arguments
if [ ${#VERSIONS[@]} -eq 0 ]; then
  echo "Error: At least one version must be specified with --versions"
  exit 1
fi

# Set up JVM using coursier
echo "Setting up JVM: $JVM"
eval "$(cs java --jvm "$JVM" --env)"
echo "Using Java: $(java -version 2>&1 | head -1)"

# Extract JVM name for results directory (replace special chars)
JVM_NAME=$(echo "$JVM" | tr ':' '-' | tr '/' '-')

# Create results directory structure
RESULTS_BASE="results/$MACHINE/$JVM_NAME"
mkdir -p "$RESULTS_BASE"

echo ""
echo "Configuration:"
echo "  Versions: ${VERSIONS[*]}"
echo "  JVM: $JVM"
echo "  Runs: $RUNS"
echo "  Machine: $MACHINE"
echo "  Results: $RESULTS_BASE/<version>/<timestamp>.json"
echo ""

# Build interleaved run order
RUN_ORDER=()
for ((run = 1; run <= RUNS; run++)); do
  for version in "${VERSIONS[@]}"; do
    RUN_ORDER+=("$version")
  done
done

echo "Run order (interleaved): ${RUN_ORDER[*]}"
echo ""

# Run benchmarks
TOTAL=${#RUN_ORDER[@]}
CURRENT=0

for version in "${RUN_ORDER[@]}"; do
  CURRENT=$((CURRENT + 1))
  TIMESTAMP=$(date +%Y%m%d-%H%M%S)
  VERSION_DIR="$RESULTS_BASE/$version"
  mkdir -p "$VERSION_DIR"
  RESULTS_FILE="$VERSION_DIR/$TIMESTAMP.json"
  RESULTS_FILE_ABS=$(realpath "$RESULTS_FILE")

  echo "[$CURRENT/$TOTAL] Running benchmarks for Scala $version..."
  echo "  Results will be written to: $RESULTS_FILE_ABS"

  sbt -Dcompiler.version="$version" \
    "clean; bench / Jmh / run -gc true -foe true -rf json -rff $RESULTS_FILE_ABS"

  echo "  Completed: $RESULTS_FILE_ABS"
  echo ""
done

echo "All benchmarks completed!"
echo "Results stored in: $RESULTS_BASE/"
