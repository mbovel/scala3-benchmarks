package bench

import org.openjdk.jmh.annotations.{Benchmark, Measurement}

class CompilationBenchmarksBig extends CompilationBenchmarks:
  @Benchmark
  def caskApp = scalac(Config.caskApp)

  @Benchmark
  def dottyUtils = scalac(Config.dottyUtil)

  @Benchmark
  def fansi = scalac(Config.fansi)

  @Benchmark
  def re2s = scalac(Config.re2s)

  @Measurement(iterations = 100)
  @Benchmark
  def scalaParserCombinators = scalac(Config.scalaParserCombinators)

  @Measurement(iterations = 80)
  @Benchmark
  def scalaYaml = scalac(Config.scalaYaml)

  @Measurement(iterations = 60)
  @Benchmark
  def scalaz = scalac(Config.scalaz)

  @Benchmark
  def sourcecode = scalac(Config.sourcecode)

  //@Benchmark def stdlib123 = scalac(Config.stdlib213)

  @Measurement(iterations = 60)
  @Benchmark
  def tastyQuery = scalac(Config.tastyQuery)
