val compilerVersion = sys.props.get("compiler.version").getOrElse("3.7.4")

ThisBuild / resolvers += Resolver.scalaNightlyRepository

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

lazy val benchSmall =
  project
    .in(file("bench-sources/small"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= Seq("-feature", "-Werror", "-deprecation"),
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchDottyUtil =
  project
    .in(file("bench-sources/dottyUtil"))
    .settings(
      scalaVersion := compilerVersion,
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchRe2s =
  project
    .in(file("bench-sources/re2s"))
    .settings(
      scalaVersion := compilerVersion,
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchScalaParserCombinators =
  project
    .in(file("bench-sources/scalaParserCombinators"))
    .settings(
      scalaVersion := compilerVersion,
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchSourcecode =
  project
    .in(file("bench-sources/sourcecode"))
    .settings(
      scalaVersion := compilerVersion,
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchScalaYaml =
  project
    .in(file("bench-sources/scalaYaml"))
    .settings(
      scalaVersion := compilerVersion,
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
  val dottyUtilDir = (benchDottyUtil / Compile / scalaSource).value
  val dottyUtilClassPath = (benchDottyUtil / Compile / dependencyClasspath).value
    .map(_.data.getAbsolutePath)
    .mkString(java.io.File.pathSeparator)
  val dottyUtilSources = (dottyUtilDir ** "*.scala").get.map(_.getAbsolutePath)

  val re2sDir = (benchRe2s / Compile / scalaSource).value
  val re2sClassPath = (benchRe2s / Compile / dependencyClasspath).value
    .map(_.data.getAbsolutePath)
    .mkString(java.io.File.pathSeparator)
  val re2sSources = (re2sDir ** "*.scala").get.map(_.getAbsolutePath)

  val scalaParserCombinatorsDir = (benchScalaParserCombinators / Compile / scalaSource).value
  val scalaParserCombinatorsClassPath = (benchScalaParserCombinators / Compile / dependencyClasspath).value
    .map(_.data.getAbsolutePath)
    .mkString(java.io.File.pathSeparator)
  val scalaParserCombinatorsSources = (scalaParserCombinatorsDir ** "*.scala").get.map(_.getAbsolutePath)

  val sourcecodeDir = (benchSourcecode / Compile / scalaSource).value
  val sourcecodeClassPath = (benchSourcecode / Compile / dependencyClasspath).value
    .map(_.data.getAbsolutePath)
    .mkString(java.io.File.pathSeparator)
  val sourcecodeSources = (sourcecodeDir ** "*.scala").get.map(_.getAbsolutePath)

  val scalaYamlDir = (benchScalaYaml / Compile / scalaSource).value
  val scalaYamlClassPath = (benchScalaYaml / Compile / dependencyClasspath).value
    .map(_.data.getAbsolutePath)
    .mkString(java.io.File.pathSeparator)
  val scalaYamlSources = (scalaYamlDir ** "*.scala").get.map(_.getAbsolutePath)

  val bigEntries = Seq(
    "dottyUtil" -> (Seq("-classpath", dottyUtilClassPath) ++ dottyUtilSources),
    "re2s" -> (Seq("-classpath", re2sClassPath) ++ re2sSources),
    "scalaParserCombinators" -> (Seq("-classpath", scalaParserCombinatorsClassPath) ++ scalaParserCombinatorsSources),
    "sourcecode" -> (Seq("-classpath", sourcecodeClassPath) ++ sourcecodeSources),
    "scalaYaml" -> (Seq("-classpath", scalaYamlClassPath) ++ scalaYamlSources),
  )

  (smallEntries ++ bigEntries).toMap
}
