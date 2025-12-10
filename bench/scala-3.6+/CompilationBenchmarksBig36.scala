package bench

import org.openjdk.jmh.annotations.{Benchmark, Measurement}

class CompilationBenchmarksBig36 extends CompilationBenchmarks:
  @Measurement(iterations = 60)
  @Benchmark
  def indigo = scalac(Config.indigo, expectedSources = 223)
