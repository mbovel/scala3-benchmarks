package bench

import java.util.concurrent.TimeUnit.MILLISECONDS

import scala.sys.process.stringToProcess

import bench.compilers.{Compiler, XsbtiCompiler}

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
  val compiler: Compiler = XsbtiCompiler

  /** Compiles the given benchmark configuration.
    * @param config benchmark configuration with sources and options
    * @param expectedSources If provided, asserts that exactly this many source files are passed
    */
  def scalac(config: BenchmarkConfig, expectedSources: Int = -1): Unit =
    if expectedSources >= 0 then
      assert(config.sources.size == expectedSources,
        s"Expected $expectedSources sources but found ${config.sources.size}")

    compiler.compile(config.sources, config.options, outDir)

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
