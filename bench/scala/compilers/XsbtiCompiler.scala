package bench.compilers

import java.io.{File, InputStream}
import java.nio.file.{Path, Paths}
import java.util.{Optional, ServiceLoader}
import java.util.function.Supplier

import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters.*

import xsbti.{AnalysisCallback2, Logger, Problem, Reporter, VirtualFile, VirtualFileRef}
import xsbti.api.{ClassLike, DependencyContext}
import xsbti.compile.{CompileProgress, CompilerInterface2, DependencyChanges, SingleOutput}

/** Compiler using the xsbti.compile.CompilerInterface2 bridge.
  *
  * This is the same interface that sbt/bloop/scala-cli use to invoke scalac.
  */
object XsbtiCompiler extends Compiler:
  def compile(sources: Seq[String], options: Seq[String], outputDir: String): Unit =
    val compilerInterface: CompilerInterface2 =
      ServiceLoader.load(classOf[CompilerInterface2]).iterator().asScala.toList.last

    val sourceFiles: Array[VirtualFile] = sources.map(s => SourceFile(Paths.get(s))).toArray
    val output = SimpleOutput(Paths.get(outputDir))
    val reporter = ErrorCollectingReporter()

    compilerInterface.run(
      sourceFiles,
      EmptyDependencyChanges,
      options.toArray,
      output,
      NoopAnalysisCallback,
      reporter,
      NoopProgress,
      NoopLogger
    )

    if reporter.hasErrors() then
      val errors = reporter.problems().map(p => s"  ${p.position().sourcePath().orElse("?")}: ${p.message()}").mkString("\n")
      throw new CompilationFailedException(s"Compilation failed with ${reporter.problems().length} errors:\n$errors")

  /** A PathBasedFile implementation for source files. */
  private class SourceFile(path: Path) extends xsbti.PathBasedFile:
    def toPath(): Path = path
    def id(): String = path.toString
    def name(): String = path.getFileName.toString
    def names(): Array[String] = Array(name())
    // Not called by the compiler bridge - it reads files directly via toPath()
    def contentHash(): Long = ???
    def input(): InputStream = ???

  /** A simple SingleOutput implementation. */
  private class SimpleOutput(outputDir: Path) extends SingleOutput:
    override def getOutputDirectory(): File = outputDir.toFile
    override def getOutputDirectoryAsPath(): Path = outputDir
    override def getSingleOutput(): Optional[File] = Optional.of(outputDir.toFile)
    override def getSingleOutputAsPath(): Optional[Path] = Optional.of(outputDir)
    override def getMultipleOutput(): Optional[Array[xsbti.compile.OutputGroup]] = Optional.empty()

  /** Empty DependencyChanges - no incremental compilation info. */
  private object EmptyDependencyChanges extends DependencyChanges:
    def isEmpty(): Boolean = true
    def modifiedBinaries(): Array[File] = Array.empty
    def modifiedLibraries(): Array[VirtualFileRef] = Array.empty
    def modifiedClasses(): Array[String] = Array.empty

  /** No-op AnalysisCallback2 - we don't need incremental analysis for benchmarks. */
  private object NoopAnalysisCallback extends AnalysisCallback2:
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
  private object NoopLogger extends Logger:
    def error(msg: Supplier[String]): Unit = ()
    def warn(msg: Supplier[String]): Unit = ()
    def info(msg: Supplier[String]): Unit = ()
    def debug(msg: Supplier[String]): Unit = ()
    def trace(exception: Supplier[Throwable]): Unit = ()

  /** Reporter that collects errors. */
  private class ErrorCollectingReporter extends Reporter:
    private val errors = ArrayBuffer[Problem]()
    private val warnings = ArrayBuffer[Problem]()

    def reset(): Unit =
      errors.clear()
      warnings.clear()

    def hasErrors(): Boolean = errors.nonEmpty
    def hasWarnings(): Boolean = warnings.nonEmpty
    def printSummary(): Unit = ()
    def problems(): Array[Problem] = (errors ++ warnings).toArray

    def log(problem: Problem): Unit =
      problem.severity() match
        case xsbti.Severity.Error => errors += problem
        case xsbti.Severity.Warn => warnings += problem
        case _ => ()

    def comment(pos: xsbti.Position, msg: String): Unit = ()

  /** No-op CompileProgress. */
  private object NoopProgress extends CompileProgress:
    override def startUnit(phase: String, unitPath: String): Unit = ()
    override def advance(current: Int, total: Int, prevPhase: String, nextPhase: String): Boolean = true

class CompilationFailedException(message: String) extends Exception(message)
