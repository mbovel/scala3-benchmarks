package bench

import bench.compilers.{DottyCompiler, XsbtiCompiler}
import org.openjdk.jmh.annotations.{Benchmark, Measurement}

class CompilationBenchmarksBig extends CompilationBenchmarks:
  @Benchmark
  def caskApp =
    assert(Config.caskApp.sources.size == 1)
    DottyCompiler.compile(Config.caskApp.sources, Config.caskApp.options, outDir)

  @Benchmark
  def dottyUtils =
    assert(Config.dottyUtil.sources.size == 34)
    DottyCompiler.compile(Config.dottyUtil.sources, Config.dottyUtil.options, outDir)

  @Benchmark
  def fansi =
    assert(Config.fansi.sources.size == 2)
    DottyCompiler.compile(Config.fansi.sources, Config.fansi.options, outDir)

  @Benchmark
  def re2s =
    assert(Config.re2s.sources.size == 17)
    DottyCompiler.compile(Config.re2s.sources, Config.re2s.options, outDir)

  @Measurement(iterations = 100)
  @Benchmark
  def scalaParserCombinators =
    assert(Config.scalaParserCombinators.sources.size == 50)
    DottyCompiler.compile(Config.scalaParserCombinators.sources, Config.scalaParserCombinators.options, outDir)

  @Measurement(iterations = 80)
  @Benchmark
  def scalaParallelCollections =
    assert(Config.scalaParallelCollections.sources.size == 86)
    DottyCompiler.compile(Config.scalaParallelCollections.sources, Config.scalaParallelCollections.options, outDir)

  @Benchmark
  def scalaToday =
    assert(Config.scalaToday.sources.size == 9)
    DottyCompiler.compile(Config.scalaToday.sources, Config.scalaToday.options, outDir)

  @Measurement(iterations = 80)
  @Benchmark
  def scalaYaml =
    assert(Config.scalaYaml.sources.size == 57)
    DottyCompiler.compile(Config.scalaYaml.sources, Config.scalaYaml.options, outDir)

  @Measurement(iterations = 60)
  @Benchmark
  def scalaz =
    assert(Config.scalaz.sources.size == 292)
    DottyCompiler.compile(Config.scalaz.sources, Config.scalaz.options, outDir)

  @Benchmark
  def sourcecode =
    assert(Config.sourcecode.sources.size == 20)
    DottyCompiler.compile(Config.sourcecode.sources, Config.sourcecode.options, outDir)

  @Measurement(iterations = 60)
  @Benchmark
  def tastyQuery =
    assert(Config.tastyQuery.sources.size == 49)
    DottyCompiler.compile(Config.tastyQuery.sources, Config.tastyQuery.options, outDir)

  @Benchmark
  def tictactoe =
    assert(Config.tictactoe.sources.size == 16)
    DottyCompiler.compile(Config.tictactoe.sources, Config.tictactoe.options, outDir)

  @Measurement(iterations = 60)
  @Benchmark
  def xsbtiTastyQuery =
    assert(Config.tastyQuery.sources.size == 49)
    XsbtiCompiler.compile(Config.tastyQuery.sources, Config.tastyQuery.options, outDir)
