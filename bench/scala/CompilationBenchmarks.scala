package bench

import java.util.concurrent.TimeUnit.MILLISECONDS

import scala.sys.process.stringToProcess

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
