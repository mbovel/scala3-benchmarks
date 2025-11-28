# Scala 3 Compiler Benchmarks

JMH benchmarks for measuring Scala 3 compiler performance.

## Quick Start

```bash
# Run benchmarks for multiple versions with interleaved runs
./run.sh --versions 3.3.4 3.7.4 --jvm temurin:21 --runs 3

# Or run manually with sbt
sbt -Dcompiler.version=3.3.4 "bench / Jmh / run -gc true -foe true"
```

## Structure

```
bench-sources/          # Benchmark source files
  helloWorld.scala      # Single-file benchmark
  sourcecode/           # Multi-file benchmark (directory)
bench/
  CompilationBenchmark.scala  # JMH suite
results/
  <machine>/<jvm>/<version>/<timestamp>.json
```

## Adding Benchmarks

1. Add a `.scala` file or directory to `bench-sources/`
2. Add a `@Benchmark` method in `CompilationBenchmark.scala`:
   ```scala
   @Benchmark def myBenchmark = scalac(Config.myBenchmark)
   ```

`Config` is auto-generated at `bench/target/scala-*/src_managed/main/bench/Config.scala` with the `scalac` arguments (classpath and sources) for each benchmark.
