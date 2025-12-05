package bench

import java.util.concurrent.TimeUnit.MILLISECONDS

import scala.sys.process.{ProcessBuilder, stringToProcess}

import dotty.tools.dotc.{Compiler, Driver, Run}
import dotty.tools.dotc.core.Contexts.{ctx, withMode, Context, ContextBase}
import dotty.tools.dotc.core.Mode
import dotty.tools.dotc.core.Types.{TermRef, Type}

import org.openjdk.jmh.annotations.{
  Benchmark,
  BenchmarkMode,
  Fork,
  Level,
  Measurement,
  OutputTimeUnit,
  Scope,
  Setup,
  State,
  Warmup,
}

@Fork(value = 1, jvmArgsPrepend = Array("-XX:+PrintCommandLineFlags", "-Xms8G", "-Xmx8G"))
@Warmup(iterations = 160)
@Measurement(iterations = 20)
@BenchmarkMode(Array(org.openjdk.jmh.annotations.Mode.SingleShotTime))
@State(Scope.Benchmark)
@OutputTimeUnit(MILLISECONDS)
class CompilationBenchmarks:
  val outDir = "out"

  /** Launches `scalac` with the given arguments. */
  def scalac(args: Seq[String]) =
    val allArgs = Array("-Werror", "-d", "out") ++ args
    val reporter = Driver().process(allArgs)
    assert(!reporter.hasErrors, "Compilation failed with errors")

  @Setup(Level.Iteration)
  def setup(): Unit =
    removeAndCreateDir(outDir)
  
  /** Removes and creates a directory. */
  def removeAndCreateDir(dir: String) =
    // Using `rm` instead of Java's API because it is better at removing the
    // whole directory atomically. Got occasional `DirectoryNotEmptyException`
    // exceptions with the Java's API.
    s"rm -rf $dir".!
    s"mkdir -p $dir".!

  @Benchmark def helloWorld = scalac(Config.helloWorld)
  @Benchmark def matchTypeBubbleSort = scalac(Config.matchTypeBubbleSort)

  // Library benchmarks
  @Benchmark def dottyUtils = scalac(Config.dottyUtil)
  @Benchmark def fansi = scalac(Config.fansi)
  @Benchmark def re2s = scalac(Config.re2s)
  @Benchmark def scalaParserCombinators = scalac(Config.scalaParserCombinators)
  @Benchmark def scalaYaml = scalac(Config.scalaYaml)
  @Benchmark def sourcecode = scalac(Config.sourcecode)

  // Benchmarks from previous suite
  @Benchmark def exhaustivityI = scalac(Config.exhaustivityI)
  @Benchmark def exhaustivityS = scalac(Config.exhaustivityS)
  @Benchmark def exhaustivityT = scalac(Config.exhaustivityT)
  @Benchmark def exhaustivityV = scalac(Config.exhaustivityV)
  @Benchmark def findRef = scalac(Config.findRef)
  @Benchmark def i1535 = scalac(Config.i1535)
  @Benchmark def i1687 = scalac(Config.i1687)
  @Benchmark def implicitCache = scalac(Config.implicitCache)
  @Benchmark def implicitInductive = scalac(Config.implicitInductive)
  @Benchmark def implicitNums = scalac(Config.implicitNums)
  @Benchmark def implicitScopeLoop = scalac(Config.implicitScopeLoop)
  @Benchmark def patmatexhaust = scalac(Config.patmatexhaust)
  @Benchmark def tuple = scalac(Config.tuple)
  @Benchmark def tuple22Apply = scalac(Config.tuple22Apply)
  @Benchmark def tuple22Cons = scalac(Config.tuple22Cons)
  @Benchmark def tuple22Creation = scalac(Config.tuple22Creation)
  @Benchmark def tuple22Size = scalac(Config.tuple22Size)
  @Benchmark def tuple22Tails = scalac(Config.tuple22Tails)
