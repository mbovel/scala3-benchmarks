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
@Warmup(iterations = 0) // default, overridden below for some benchmarks
@Measurement(iterations = 180) // default, overridden below for some benchmarks
@BenchmarkMode(Array(org.openjdk.jmh.annotations.Mode.SingleShotTime))
@State(Scope.Benchmark)
@OutputTimeUnit(MILLISECONDS)
class CompilationBenchmarks:
  val scalaVersion = util.Properties.versionNumberString
  val outDir = "out"

  /** Launches `scalac` with the given arguments. */
  def scalac(args: Seq[String]) =
    val allArgs = Array("-d", "out") ++ args
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

  @Benchmark def sourcecode = scalac(Config.sourcecode)

  @Benchmark def re2s = scalac(Config.re2s)

  @Benchmark def dottyUtils = scalac(Config.dottyUtil)

  @Benchmark def scalaYaml = scalac(Config.scalaYaml)
