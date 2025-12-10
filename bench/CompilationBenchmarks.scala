package bench

import java.util.concurrent.TimeUnit.MILLISECONDS

import scala.sys.process.{stringToProcess, ProcessBuilder}

import dotty.tools.dotc.{Compiler, Driver, Run}
import dotty.tools.dotc.core.Contexts.{ctx, withMode, Context, ContextBase}
import dotty.tools.dotc.core.Mode
import dotty.tools.dotc.core.Types.{TermRef, Type}

import org.openjdk.jmh.annotations.{
  BenchmarkMode,
  Fork,
  Level,
  Measurement,
  OutputTimeUnit,
  Scope,
  Setup,
  State,
  Warmup
}

@Fork(value = 1, jvmArgsPrepend = Array("-XX:+PrintCommandLineFlags", "-Xms8G", "-Xmx8G", "--sun-misc-unsafe-memory-access=allow"))
@Warmup(iterations = 0)
@Measurement(iterations = 180)
@BenchmarkMode(Array(org.openjdk.jmh.annotations.Mode.SingleShotTime))
@State(Scope.Benchmark)
@OutputTimeUnit(MILLISECONDS)
abstract class CompilationBenchmarks:
  val outDir = "out"

  /** Launches `scalac` with the given arguments.
    * @param args Compiler arguments including source files
    * @param expectedSources If provided, asserts that exactly this many .scala files are passed
    */
  def scalac(args: Seq[String], expectedSources: Int = -1) =
    if expectedSources >= 0 then
      val sourceCount = args.count(_.endsWith(".scala"))
      assert(sourceCount == expectedSources,
        s"Expected $expectedSources sources but found $sourceCount")
    val allArgs = Array("-d", "out") ++ args
    val reporter = Driver().process(allArgs)
    assert(!reporter.hasErrors, "Compilation failed with errors: " + reporter.allErrors)

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
