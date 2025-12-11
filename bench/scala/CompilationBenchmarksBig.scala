package bench

import org.openjdk.jmh.annotations.{Benchmark, Measurement}

class CompilationBenchmarksBig extends CompilationBenchmarks:
  @Benchmark
  def caskApp = scalac(Config.caskApp, expectedSources = 1)

  @Benchmark
  def dotty = scalac(Config.dotty, expectedSources = 587)

  @Benchmark
  def fansi = scalac(Config.fansi, expectedSources = 2)

  @Benchmark
  def re2s = scalac(Config.re2s, expectedSources = 17)

  @Measurement(iterations = 100)
  @Benchmark
  def scalaParserCombinators = scalac(Config.scalaParserCombinators, expectedSources = 50)

  @Measurement(iterations = 80)
  @Benchmark
  def scalaParallelCollections = scalac(Config.scalaParallelCollections, expectedSources = 86)

  @Benchmark
  def scalaToday = scalac(Config.scalaToday, expectedSources = 9)

  @Measurement(iterations = 80)
  @Benchmark
  def scalaYaml = scalac(Config.scalaYaml, expectedSources = 57)

  @Measurement(iterations = 60)
  @Benchmark
  def scalaz = scalac(Config.scalaz, expectedSources = 292)

  @Benchmark
  def sourcecode = scalac(Config.sourcecode, expectedSources = 20)

  //@Benchmark def stdlib123 = scalac(Config.stdlib213)

  @Measurement(iterations = 60)
  @Benchmark
  def tastyQuery = scalac(Config.tastyQuery, expectedSources = 49)

  @Benchmark
  def tictactoe = scalac(Config.tictactoe, expectedSources = 16)
