package bench.compilers

import dotty.tools.dotc.Driver

/** Compiler that directly calls Dotty's Driver API.
  *
  * This is a simpler approach that bypasses the sbt bridge interface.
  */
object DottyCompiler extends Compiler:
  def compile(sources: Seq[String], options: Seq[String], outputDir: String): Unit =
    val allArgs = Array("-d", outputDir) ++ options ++ sources
    val reporter = Driver().process(allArgs)
    if reporter.hasErrors then
      throw new CompilationFailedException(
        s"Compilation failed with errors: ${reporter.allErrors.mkString("\n")}"
      )
