# Scala 3 Compiler Benchmarks

JMH benchmarks for measuring Scala 3 compiler performance.

## Benchmarks

**Real-world code** (multi-file):
- [scala-parser-combinators](https://github.com/scala/scala-parser-combinators)
- [scala-yaml](https://github.com/VirtusLab/scala-yaml)
- [sourcecode](https://github.com/com-lihaoyi/sourcecode)
- [dottyUtil](https://github.com/scala/scala3/tree/main/compiler/src/dotty/tools/dotc/util): some utility code extracted from the Scala 3 compiler
- [re2s](https://github.com/twitter/rsc/tree/1d2b8962604206b1328e94257885117fd252bd23/examples/re2s/src/main/scala/java/util/regex): a regex implementation, originally used to test [reasonable-scala](https://github.com/twitter/reasonable-scala/)


**Synthetic benchmarks** (single-file): The remaining benchmarks target specific compiler aspects (pattern matching, implicit resolution, inlining, etc.). Most are adapted from the previous benchmark suite.

## Quick Start

```bash
# Run benchmarks for multiple versions with interleaved runs
./run.sh --versions 3.3.4 3.7.4 --jvm temurin:21 --runs 3

# Or run manually with sbt
sbt -Dcompiler.version=3.3.4 "clean; bench / Jmh / run -gc true -foe true"
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

Benchmarks should:

- compile with all version between Scala 3.3.4 and the latest
- compile in ~100ms-10s range (after warmup)
- not require complex setup
- Ideally not require external dependencies

Potential future benchmarks:

- [scala-parallel-collections](https://github.com/scala/scala-parallel-collections) (6773 LOC)
- [cats core](https://github.com/typelevel/cats/tree/main/core/src/main) (30,673 LOC)
- [scalaz](https://github.com/scalaz/scalaz)
- Dotty (waiting 3.8)
- Scala Standard Library (waiting 3.8)
- [quicklens](https://github.com/softwaremill/quicklens)  (waiting 3.8)

To add a new benchmark:

1. Add a `.scala` file or directory to `bench-sources/`
2. Add a `@Benchmark` method in `CompilationBenchmark.scala`:
   ```scala
   @Benchmark def myBenchmark = scalac(Config.myBenchmark)
   ```

`Config` is auto-generated at `bench/target/scala-*/src_managed/main/bench/Config.scala` with the `scalac` arguments (classpath and sources) for each benchmark.

## Using JMH's profilers

Examples of using JMH's built-in profilers: [jmh/samples/JMHSample_35_Profilers.java](https://github.com/openjdk/jmh/blob/master/jmh-samples/src/main/java/org/openjdk/jmh/samples/JMHSample_35_Profilers.java).

### Async Profiler

Flame graphs can be generated using [async-profiler](https://github.com/async-profiler/async-profiler). Example command:

```bash
sbt -Dcompiler.version=3.7.4 "clean; bench / Jmh / run -gc true -foe true -prof \"async:libPath=../async-profiler-4.2.1-macos/lib/libasyncProfiler.dylib;output=flamegraph;dir=profile-results;include=CompilationBenchmarks.scalac\" helloWorld"
```

Replace `3.7.4`, `../async-profiler-4.2.1-macos/lib/libasyncProfiler.dylib` and `helloWorld` with the desired Scala version, path to the async profiler library, and benchmark name respectively. Read more at [markrmiller/jmh-profilers.md](https://gist.github.com/markrmiller/a04f5c734fad879f688123bc312c21af#using-jmh-with-the-async-profiler).

The default sampling interval is 10ms. It can be changed by adding the `interval`, which is specified in nanoseconds. For example, to set the interval to 1ms, use `interval=1000000`.

Async-profiler options reference [async-profiler/docs/ProfilerOptions.md](https://github.com/async-profiler/async-profiler/blob/master/docs/ProfilerOptions.md).
