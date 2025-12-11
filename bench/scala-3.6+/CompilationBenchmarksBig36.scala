package bench

import bench.compilers.DottyCompiler
import org.openjdk.jmh.annotations.{Benchmark, Measurement}

class CompilationBenchmarksBig36 extends CompilationBenchmarks:
  @Measurement(iterations = 60)
  @Benchmark
  def indigo =
    assert(Config.indigo.sources.size == 223)
    DottyCompiler.compile(Config.indigo.sources, Config.indigo.options, outDir)
