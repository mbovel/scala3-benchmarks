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
      scalacOptions ++= Seq("-feature", "-Werror"),
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
