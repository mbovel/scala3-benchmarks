package bench.compilers

import java.nio.file.Path

/** Interface for invoking the Scala compiler. */
trait Compiler:
  /** Compiles the given source files with the given options.
    *
    * @param sources paths to .scala or .java files
    * @param options compiler options (excluding -d)
    * @param outputDir directory where compiled classes will be written
    */
  def compile(sources: Seq[String], options: Seq[String], outputDir: String): Unit
