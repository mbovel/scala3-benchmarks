# Scala 3 Compiler Benchmarks

JMH benchmarks for measuring Scala 3 compiler performance.

## Benchmarks

**Real-world code** (multi-file): Sources are vendored (copied directly into this repository) and fixed to compile without errors or warnings across all Scala versions from 3.3.7 to nightly. Fixed versions ensure comparable benchmark results.

| Project | Version | LOC | Dependencies | Tests | Features |
|---------|---------|----:|--------------|:-----:|----------|
| [dottyUtil](https://github.com/scala/scala3/tree/main/compiler/src/dotty/tools/dotc/util) | 6462d7d7 | 2209 | none | no | inline |
| [fansi](https://github.com/com-lihaoyi/fansi) | 0.5.1 | 960 | sourcecode<br/>utest 0.8.3 | yes | implicits |
| [re2s](https://github.com/twitter/rsc/tree/1d2b8962604206b1328e94257885117fd252bd23/examples/re2s/src/main/scala/java/util/regex) | 1d2b8962 | 9021 | none | no | implicits |
| [scala-parser-combinators](https://github.com/scala/scala-parser-combinators) | 2.4.0 | 1107 | none | no | implicits |
| [scala-yaml](https://github.com/VirtusLab/scala-yaml) | 0.3.1 | 6473 | pprint 0.9.4<br/>munit 1.2.0 | yes | macros, implicits |
| [sourcecode](https://github.com/com-lihaoyi/sourcecode) | 0.4.4 | 638 | none | yes | macros, inline, implicits |

LOC = lines of Scala code (reported by [cloc](https://github.com/AlDanial/cloc)). Features = notable usage of: inline, macros, implicits, match types, tuples.

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
bench-sources/
  small/                # Synthetic single-file benchmarks
    helloWorld.scala
    ...
  dottyUtil/            # Real-world multi-file benchmarks (each is an SBT subproject)
  fansi/
  re2s/
  scalaParserCombinators/
  scalaYaml/
  sourcecode/
bench/
  CompilationBenchmarks.scala  # JMH suite
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

1. Add a `.scala` file to `bench-sources/small/`, or create a new SBT subproject in `bench-sources/` for multi-file benchmarks
2. Add a `@Benchmark` method in `CompilationBenchmarks.scala`:
   ```scala
   @Benchmark def myBenchmark = scalac(Config.myBenchmark)
   ```

`Config` is auto-generated at `bench/target/scala-*/src_managed/main/bench/Config.scala` with the `scalac` arguments (classpath and sources) for each benchmark.

## Running Tests

Some benchmarks (fansi, sourcecode, scalaYaml) include tests from their upstream repositories. To avoid a separate SBT test configuration, all source files (including tests) are compiled together under `Compile`, and test entry points are provided as main classes:

```bash
sbt "benchSourcecode/runMain sourcecode.Main"
sbt "benchFansi/runMain test.fansi.Main"
sbt "benchScalaYaml/runMain org.virtuslab.yaml.test.TestRunner"
```

See individual benchmark READMEs for details.

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

## Known Issues

Under Java 25, the following warning is printed during benchmark runs:

```
[info] WARNING: A terminally deprecated method in sun.misc.Unsafe has been called
[info] WARNING: sun.misc.Unsafe::objectFieldOffset has been called by org.openjdk.jmh.util.Utils (file:/home/runner/work/scala3-benchmarks/scala3-benchmarks/target/bg-jobs/sbt_accfab51/target/09a4797f/1296d6b9/jmh-core-1.37.jar)
[info] WARNING: Please consider reporting this to the maintainers of class org.openjdk.jmh.util.Utils
[info] WARNING: sun.misc.Unsafe::objectFieldOffset will be removed in a future release
```

It can be ignored for now. It is fixed by https://github.com/openjdk/jmh/pull/140, which will be included in the next JMH release.
