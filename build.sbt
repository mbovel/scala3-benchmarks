val compilerVersion = sys.props.get("compiler.version").getOrElse("3.7.4")

lazy val bench =
  project
    .in(file("bench"))
    .settings(
      scalaVersion := compilerVersion,
      libraryDependencies ++= Seq(
        "org.scala-lang" %% "scala3-compiler" % compilerVersion,
      ),
      Compile / scalaSource := baseDirectory.value,
      Compile / sourceGenerators += generateBenchmarkConfig.taskValue,
    )
    .enablePlugins(JmhPlugin)

lazy val benchSources =
  project
    .in(file("bench-sources"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= Seq("-feature", "-Xfatal-warnings"),
      Compile / scalaSource := baseDirectory.value,
    )

def generateBenchmarkConfig = Def.task {
  val configFile = (Compile / sourceManaged).value / "bench" / "Config.scala"
  val benchmarks = benchmarkConfigs.value
  val members = benchmarks.map { case (name, args) =>
    val argsSeq = args.map(a => s""""$a"""").mkString("Seq(", ", ", ")")
    s"  val $name = $argsSeq"
  }.mkString("\n")
  val content =
    s"""package bench
       |
       |object Config:
       |$members
       |""".stripMargin
  IO.write(configFile, content)
  Seq(configFile)
}

def benchmarkConfigs = Def.task {
  val benchSourcesDir = (benchSources / Compile / scalaSource).value
  val classPath = (benchSources / Compile / dependencyClasspath).value
    .map(_.data.getAbsolutePath)
    .mkString(java.io.File.pathSeparator)

  val entries = benchSourcesDir.listFiles.toSeq.flatMap { entry =>
    if (entry.isFile && entry.getName.endsWith(".scala")) {
      // Single .scala file: use filename (without extension) as benchmark name
      val name = entry.getName.stripSuffix(".scala")
      Some(name -> Seq("-classpath", classPath, entry.getAbsolutePath))
    } else if (entry.isDirectory) {
      // Directory: collect all .scala files inside recursively
      val sources = (entry ** "*.scala").get.map(_.getAbsolutePath)
      if (sources.nonEmpty) {
        val name = entry.getName
        Some(name -> (Seq("-classpath", classPath) ++ sources))
      } else None
    } else None
  }
  entries.toMap
}

/*
def bigBenchmarkConfigs = Def.task {
  val baseDir = (ThisBuild / baseDirectory).value
  Map(
    "munit" -> munitBenchmarkConfig(baseDir)
  )
}

def munitBenchmarkConfig(baseDir: File): Seq[String] = {
  val munitDir = baseDir / "big-benchmarks" / "munit"
  val classPath = runSbt(munitDir, s"++$compilerVersion; export munitJVM/Compile/fullClasspath")
  val scalacOptions = parseSbtList(runSbt(munitDir, s"++$compilerVersion; print munitJVM/Compile/scalacOptions"))
  val sources = parseSbtList(runSbt(munitDir, s"++$compilerVersion; print munitJVM/Compile/sources"))
  Seq("-classpath", classPath) ++ scalacOptions ++ sources
}

/** Parses an SBT list produced by the `print` command into a sequence of strings. */
def parseSbtList(output: String): Seq[String] =
  output.linesIterator
    .filter(_.startsWith("* "))
    .map(_.stripPrefix("* "))
    .toSeq

val runSbtCache = collection.mutable.Map.empty[(File, String), String]

/** Runs an SBT command in the given working directory and returns its output. */
def runSbt(cwd: File, command: String): String = {
  runSbtCache.getOrElseUpdate((cwd, command), {
    println(s"Running SBT command in $cwd: $command")
    sys.process.Process(
      Seq(
        "sbt",
        "--batch",
        "--error",
        "-Dsbt.supershell=false",
        "-Dsbt.log.noformat=true",
        command
      ),
      cwd
    ).!!.trim
  })
}
*/
