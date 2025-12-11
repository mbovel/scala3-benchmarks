package bench

import java.io.{File, InputStream}
import java.nio.file.{Files, Path, Paths}
import java.util.{Optional, ServiceLoader}
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.function.Supplier

import scala.jdk.CollectionConverters.*
import scala.sys.process.stringToProcess

import xsbti.{AnalysisCallback2, Logger, Problem, Reporter, VirtualFile, VirtualFileRef}
import xsbti.api.{ClassLike, DependencyContext}
import xsbti.compile.{CompileProgress, CompilerInterface2, DependencyChanges, Output, SingleOutput}

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

/** A PathBasedFile implementation for source files. */
class SourceFile(path: Path) extends xsbti.PathBasedFile:
  private val content = Files.readAllBytes(path)
  def toPath(): Path = path
  def id(): String = path.toString
  def name(): String = path.getFileName.toString
  def names(): Array[String] = Array(name())
  def contentHash(): Long = java.util.Arrays.hashCode(content)
  def input(): InputStream = new java.io.ByteArrayInputStream(content)

/** A simple SingleOutput implementation. */
class SimpleOutput(outputDir: Path) extends SingleOutput:
  override def getOutputDirectory(): File = outputDir.toFile
  override def getOutputDirectoryAsPath(): Path = outputDir
  override def getSingleOutput(): Optional[File] = Optional.of(outputDir.toFile)
  override def getSingleOutputAsPath(): Optional[Path] = Optional.of(outputDir)
  override def getMultipleOutput(): Optional[Array[xsbti.compile.OutputGroup]] = Optional.empty()

/** Empty DependencyChanges - no incremental compilation info. */
object EmptyDependencyChanges extends DependencyChanges:
  def isEmpty(): Boolean = true
  def modifiedBinaries(): Array[File] = Array.empty
  def modifiedLibraries(): Array[VirtualFileRef] = Array.empty
  def modifiedClasses(): Array[String] = Array.empty

/** No-op AnalysisCallback2 - we don't need incremental analysis for benchmarks. */
object NoopAnalysisCallback extends AnalysisCallback2:
  // Deprecated File-based methods
  override def startSource(source: File): Unit = ()
  override def binaryDependency(onBinaryEntry: File, onBinaryClassName: String, fromClassName: String, fromSourceFile: File, context: xsbti.api.DependencyContext): Unit = ()
  override def generatedNonLocalClass(source: File, classFile: File, binaryClassName: String, srcClassName: String): Unit = ()
  override def generatedLocalClass(source: File, classFile: File): Unit = ()
  override def api(sourceFile: File, classApi: ClassLike): Unit = ()
  override def mainClass(sourceFile: File, className: String): Unit = ()
  // Path-based methods
  override def startSource(source: VirtualFile): Unit = ()
  override def classDependency(onClassName: String, sourceClassName: String, context: DependencyContext): Unit = ()
  override def binaryDependency(onBinaryEntry: Path, onBinaryClassName: String, fromClassName: String, fromSourceFile: VirtualFileRef, context: DependencyContext): Unit = ()
  override def generatedNonLocalClass(source: VirtualFileRef, classFile: Path, binaryClassName: String, srcClassName: String): Unit = ()
  override def generatedLocalClass(source: VirtualFileRef, classFile: Path): Unit = ()
  override def api(sourceFile: VirtualFileRef, classApi: ClassLike): Unit = ()
  override def mainClass(sourceFile: VirtualFileRef, className: String): Unit = ()
  override def usedName(className: String, name: String, useScopes: java.util.EnumSet[xsbti.UseScope]): Unit = ()
  override def problem(what: String, pos: xsbti.Position, msg: String, severity: xsbti.Severity, reported: Boolean): Unit = ()
  // AnalysisCallback2 method
  override def problem2(what: String, pos: xsbti.Position, msg: String, severity: xsbti.Severity, reported: Boolean, rendered: Optional[String], diagnosticCode: Optional[xsbti.DiagnosticCode], diagnosticRelatedInformation: java.util.List[xsbti.DiagnosticRelatedInformation], actions: java.util.List[xsbti.Action]): Unit = ()
  override def dependencyPhaseCompleted(): Unit = ()
  override def apiPhaseCompleted(): Unit = ()
  override def enabled(): Boolean = false
  override def classesInOutputJar(): java.util.Set[String] = java.util.Collections.emptySet()
  override def isPickleJava(): Boolean = false
  override def getPickleJarPair(): Optional[xsbti.T2[Path, Path]] = Optional.empty()

/** Simple logger that does nothing. */
object NoopLogger extends Logger:
  def error(msg: Supplier[String]): Unit = ()
  def warn(msg: Supplier[String]): Unit = ()
  def info(msg: Supplier[String]): Unit = ()
  def debug(msg: Supplier[String]): Unit = ()
  def trace(exception: Supplier[Throwable]): Unit = ()

/** Reporter that collects errors. */
class ErrorCollectingReporter extends Reporter:
  private var errors: List[Problem] = Nil
  private var warnings: List[Problem] = Nil

  def reset(): Unit =
    errors = Nil
    warnings = Nil

  def hasErrors(): Boolean = errors.nonEmpty
  def hasWarnings(): Boolean = warnings.nonEmpty
  def printSummary(): Unit = ()
  def problems(): Array[Problem] = (errors ++ warnings).toArray

  def log(problem: Problem): Unit =
    problem.severity() match
      case xsbti.Severity.Error => errors = problem :: errors
      case xsbti.Severity.Warn => warnings = problem :: warnings
      case _ => ()

  def comment(pos: xsbti.Position, msg: String): Unit = ()

/** No-op CompileProgress. */
object NoopProgress extends CompileProgress:
  override def startUnit(phase: String, unitPath: String): Unit = ()
  override def advance(current: Int, total: Int, prevPhase: String, nextPhase: String): Boolean = true

@Fork(value = 1, jvmArgsPrepend = Array("-XX:+PrintCommandLineFlags", "-Xms8G", "-Xmx8G", "--sun-misc-unsafe-memory-access=allow"))
@Warmup(iterations = 0)
@Measurement(iterations = 180)
@BenchmarkMode(Array(org.openjdk.jmh.annotations.Mode.SingleShotTime))
@State(Scope.Benchmark)
@OutputTimeUnit(MILLISECONDS)
abstract class CompilationBenchmarks:
  val outDir = "out"

  // Load compiler interface once via ServiceLoader
  private val compilerInterface: CompilerInterface2 =
    ServiceLoader.load(classOf[CompilerInterface2]).iterator().asScala.toList.last

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

    // Convert source files to VirtualFile array
    val sources: Array[VirtualFile] = sourceFiles.map(f => SourceFile(Paths.get(f))).toArray

    // Prepare options (note: -d is added by CompilerBridgeDriver from the Output object)
    val allOptions = options.toArray

    // Create output
    val output = SimpleOutput(Paths.get(outDir))

    // Create reporter
    val reporter = ErrorCollectingReporter()

    // Run the compiler through the bridge interface
    compilerInterface.run(
      sources,
      EmptyDependencyChanges,
      allOptions,
      output,
      NoopAnalysisCallback,
      reporter,
      NoopProgress,
      NoopLogger
    )

    if reporter.hasErrors() then
      val errors = reporter.problems().map(p => s"  ${p.position().sourcePath().orElse("?")}: ${p.message()}").mkString("\n")
      throw new AssertionError(s"Compilation failed with ${reporter.problems().length} errors:\n$errors")

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
