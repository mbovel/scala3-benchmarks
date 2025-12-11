package bench

import java.nio.file.{Path, Paths}
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

  private val compiler = XsbtiCompiler(Paths.get(outDir))

  /** Launches `scalac` with the given arguments.
    * @param args Compiler arguments including source files
    * @param expectedSources If provided, asserts that exactly this many .scala/.java files are passed
    */
  def scalac(args: Seq[String], expectedSources: Int = -1) =
    // Separate source files from options
    val sourceFiles = args.filter(a => a.endsWith(".scala") || a.endsWith(".java"))
    val options = args.filterNot(a => a.endsWith(".scala") || a.endsWith(".java"))

    // Sanity check on number of source files
    if expectedSources >= 0 then
      assert(sourceFiles.size == expectedSources,
        s"Expected $expectedSources sources but found ${sourceFiles.size}")

    compiler.compile(sourceFiles.map(Paths.get(_)), options)

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
