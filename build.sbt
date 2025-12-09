val compilerVersion = sys.props.get("compiler.version").getOrElse("3.7.4")

val sharedScalacOptions = Seq("-feature", "-Werror", "-deprecation")

ThisBuild / resolvers += Resolver.scalaNightlyRepository

lazy val bench =
  project
    .in(file("bench"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      libraryDependencies ++= Seq(
        "org.scala-lang" %% "scala3-compiler" % compilerVersion,
      ),
      Compile / scalaSource := baseDirectory.value,
      Compile / sourceGenerators += generateBenchmarkConfig.taskValue,
    )
    .enablePlugins(JmhPlugin)

lazy val benchSmall =
  project
    .in(file("bench-sources/small"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchDottyUtil =
  project
    .in(file("bench-sources/dottyUtil"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchRe2s =
  project
    .in(file("bench-sources/re2s"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchScalaParserCombinators =
  project
    .in(file("bench-sources/scalaParserCombinators"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= Seq("-feature", "-deprecation"),
      libraryDependencies ++= Seq(
        "junit" % "junit" % "4.13.2",
        "com.github.sbt" % "junit-interface" % "0.13.3",
      ),
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchSourcecode =
  project
    .in(file("bench-sources/sourcecode"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchScalaYaml =
  project
    .in(file("bench-sources/scalaYaml"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "pprint" % "0.9.4",
        "org.scalameta" %% "munit" % "1.2.0",
      ),
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchFansi =
  project
    .in(file("bench-sources/fansi"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "sourcecode" % "0.4.0",
        "com.lihaoyi" %% "utest" % "0.8.3",
      ),
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

def bigBenchmarkConfig(project: Project) = Def.task {
  val name = project.base.getName
  val dir = (project / Compile / scalaSource).value
  val classPath = (project / Compile / dependencyClasspath).value
    .map(_.data.getAbsolutePath)
    .mkString(java.io.File.pathSeparator)
  val sources = (dir ** "*.scala").get.map(_.getAbsolutePath)
  name -> (Seq("-classpath", classPath) ++ sources)
}

def benchmarkConfigs = Def.task {
  // Small benchmarks: single .scala files
  val smallDir = (benchSmall / Compile / scalaSource).value
  val smallClassPath = (benchSmall / Compile / dependencyClasspath).value
    .map(_.data.getAbsolutePath)
    .mkString(java.io.File.pathSeparator)

  val smallEntries = smallDir.listFiles.toSeq.flatMap { entry =>
    if (entry.isFile && entry.getName.endsWith(".scala")) {
      val name = entry.getName.stripSuffix(".scala")
      Some(name -> Seq("-classpath", smallClassPath, entry.getAbsolutePath))
    } else None
  }

  // Big benchmarks: each has its own subproject
  val bigEntries = Seq(
    bigBenchmarkConfig(benchDottyUtil).value,
    bigBenchmarkConfig(benchFansi).value,
    bigBenchmarkConfig(benchRe2s).value,
    bigBenchmarkConfig(benchScalaParserCombinators).value,
    bigBenchmarkConfig(benchSourcecode).value,
    bigBenchmarkConfig(benchScalaYaml).value,
  )

  (smallEntries ++ bigEntries).toMap
}
