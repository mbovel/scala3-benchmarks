val compilerVersion = sys.props.get("compiler.version").getOrElse("3.8.1-RC1-bin-20251209-07883c1-NIGHTLY")

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
      Compile / scalaSource := baseDirectory.value / "scala",
      Compile / sourceGenerators += generateBenchmarkConfig.taskValue,
      Compile / unmanagedSourceDirectories ++= {
        val base = (Compile / baseDirectory).value
        val sv = scalaVersion.value
        val dirs =
          if (VersionNumber(sv).matchesSemVer(SemanticSelector(">=3.6"))) Seq(base / s"scala-3.6+")
          else Seq()
        dirs
      },
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

//lazy val benchStdlib213 =
//  project
//    .in(file("bench-sources/stdlib213"))
//    .settings(
//      scalaVersion := compilerVersion,
//      scalacOptions ++= sharedScalacOptions ++ Seq("-nowarn", "-language:implicitConversions", "-source", "3.3"),
//      Compile / scalaSource := baseDirectory.value / "src" / "library",
//    )

lazy val benchScalaz =
  project
    .in(file("bench-sources/scalaz"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions ++ Seq(
        "-nowarn",
        "-source",
        "3.0",
        kindProjectorFlag(compilerVersion),
        "-language:implicitConversions",
      ),
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
      scalacOptions ++= sharedScalacOptions,
      libraryDependencies += "junit" % "junit" % "4.13.2",
      libraryDependencies += "com.github.sbt" % "junit-interface" % "0.13.3" % Test,
      Compile / scalaSource := baseDirectory.value / "shared" / "src" / "main" / "scala",
      Compile / unmanagedSourceDirectories ++= Seq(
        baseDirectory.value / "shared" / "src" / "main" / "scala-2.13+",
        baseDirectory.value / "jvm" / "src" / "main" / "scala",
      ),
      Test / scalaSource := baseDirectory.value / "test" / "scala",
    )

lazy val benchScalaParallelCollections =
  project
    .in(file("bench-sources/scalaParallelCollections"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      Compile / scalaSource := baseDirectory.value / "scala",
    )

lazy val benchSourcecode =
  project
    .in(file("bench-sources/sourcecode"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      Compile / scalaSource := baseDirectory.value / "src",
      Compile / unmanagedSourceDirectories += baseDirectory.value / "src-3",
      Test / scalaSource := baseDirectory.value / "test" / "src",
      Test / unmanagedSourceDirectories += baseDirectory.value / "test" / "src-3",
      Test / test := (Test / runMain).toTask(" sourcecode.Tests").value,
    )

lazy val benchTastyQuery =
  project
    .in(file("bench-sources/tastyQuery"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions ++ Seq("-Yexplicit-nulls", "-Wconf:msg=Unnecessary .nn:s"),
      Compile / scalaSource := baseDirectory.value / "tasty-query",
    )

lazy val benchScalaYaml =
  project
    .in(file("bench-sources/scalaYaml"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "pprint" % "0.9.4",
        "org.scalameta" %% "munit" % "1.2.0" % Test,
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      Compile / scalaSource := baseDirectory.value / "core" / "shared" / "src" / "main" / "scala",
      Compile / unmanagedSourceDirectories += baseDirectory.value / "core" / "shared" / "src" / "main" / "scala-3",
      Test / scalaSource := baseDirectory.value / "core" / "shared" / "test" / "scala",
      Test / unmanagedSourceDirectories ++= Seq(
        baseDirectory.value / "core" / "shared" / "test" / "scala-3",
        baseDirectory.value / "jvm" / "src" / "test",
      ),
    )

lazy val benchFansi =
  project
    .in(file("bench-sources/fansi"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "sourcecode" % "0.4.0",
        "com.lihaoyi" %% "utest" % "0.8.3" % Test,
      ),
      testFrameworks += new TestFramework("utest.runner.Framework"),
      Compile / scalaSource := baseDirectory.value / "src",
      Test / scalaSource := baseDirectory.value / "test" / "src",
    )

lazy val benchCaskApp =
  project
    .in(file("bench-sources/caskApp"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      libraryDependencies ++= Seq(
        "com.lihaoyi" %% "cask" % "0.10.2",
        "com.lihaoyi" %% "scalatags" % "0.13.1",
      ),
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchScalaToday =
  project
    .in(file("bench-sources/scalaToday"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      libraryDependencies ++= Seq(
        "com.softwaremill.sttp.tapir" %% "tapir-netty-server-sync" % "1.10.8",
        "com.softwaremill.sttp.tapir" %% "tapir-files" % "1.10.8",
        "com.softwaremill.sttp.tapir" %% "tapir-json-upickle" % "1.10.8",
        "com.softwaremill.ox" %% "core" % "0.2.1",
        "com.outr" %% "scribe" % "3.15.0",
        "com.outr" %% "scribe-slf4j2" % "3.15.0",
        "com.augustnagro" %% "magnum" % "1.2.0",
        "com.augustnagro" %% "magnumpg" % "1.2.0",
        "com.zaxxer" % "HikariCP" % "5.1.0",
        "org.postgresql" % "postgresql" % "42.7.3",
        "org.flywaydb" % "flyway-core" % "10.15.0",
        "org.flywaydb" % "flyway-database-postgresql" % "10.15.0",
        "com.lihaoyi" %% "fastparse" % "3.1.0",
        "com.lihaoyi" %% "scalatags" % "0.13.1",
        "com.lihaoyi" %% "pprint" % "0.9.0",
        "org.virtuslab" %% "besom-cfg" % "0.1.0",
        "org.scala-lang" %% "toolkit" % "0.4.0",
      ),
      Compile / scalaSource := baseDirectory.value,
    )

lazy val benchTictactoe =
  project
    .in(file("bench-sources/tictactoe"))
    .settings(
      scalaVersion := compilerVersion,
      scalacOptions ++= sharedScalacOptions,
      libraryDependencies ++= Seq(
        "org.typelevel" %% "cats-effect" % "3.5.7",
        "org.typelevel" %% "cats-core" % "2.12.0",
        "org.typelevel" %% "munit-cats-effect" % "2.0.0" % Test,
      ),
      testFrameworks += new TestFramework("munit.Framework"),
      Compile / scalaSource := baseDirectory.value / "src",
      Test / scalaSource := baseDirectory.value / "test",
    )

// Scala.js version for cross-platform Scala.js dependencies
// Need 1.20.1+ for LinkingInfo.linkTimeIf used by ultraviolet
val scalaJSVersion = "1.20.1"

lazy val benchIndigo =
  if (VersionNumber(compilerVersion).matchesSemVer(SemanticSelector(">=3.6")))
    project
      .in(file("bench-sources/indigo"))
      .settings(
        scalaVersion := compilerVersion,
        scalacOptions ++= sharedScalacOptions ++ Seq(
          "-scalajs",
          "-language:strictEquality",
          "-language:implicitConversions",
          "-Wconf:msg=Implicit parameters should be provided:s",
          "-Wconf:msg=Extension method toString will never be selected:s",
          "-Wconf:msg=Discarded non-Unit value:s",
        ),
        libraryDependencies ++= Seq(
          "org.scala-js" % "scalajs-library_2.13" % scalaJSVersion,
          "org.scala-js" % "scalajs-javalib" % scalaJSVersion,
          "org.scala-lang" % "scala3-library_sjs1_3" % compilerVersion,
          "org.scala-js" % "scalajs-dom_sjs1_3" % "2.8.0",
          "org.scala-js" % "scala-js-macrotask-executor_sjs1_3" % "1.1.1",
          "io.indigoengine" % "ultraviolet_sjs1_3" % "0.6.0",
        ),
        Compile / scalaSource := baseDirectory.value / "src",
      )
  else
    // Empty project...
    project
      .in(file("bench-sources/indigo"))
      .settings(
        // Disable everything
        Compile / sources := Seq.empty,
        Test / sources := Seq.empty,
        publish / skip := true,
      )

def kindProjectorFlag(scalaVersion: String): String =
  if (VersionNumber(scalaVersion).matchesSemVer(SemanticSelector("<=3.4")))
    "-Ykind-projector"
  else
    "-Xkind-projector"

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

def bigBenchmarkConfig(project: Project, includeTests: Boolean = false) = Def.task {
  val name = project.base.getName
  val compileSourceDirs = (project / Compile / unmanagedSourceDirectories).value.distinct
  val compileSources = compileSourceDirs.flatMap(dir => (dir ** "*.scala").get)
  val testSourceDirs = (project / Test / unmanagedSourceDirectories).value.distinct
  val testSources = if (includeTests) testSourceDirs.flatMap(dir => (dir ** "*.scala").get) else Seq.empty
  val allSources = (compileSources ++ testSources).distinct.map(_.getAbsolutePath)
  val testClasspath = (project / Test / dependencyClasspath).value
  val compileClasspath = (project / Compile / dependencyClasspath).value
  val classPath = (if (includeTests) testClasspath else compileClasspath)
    .map(_.data.getAbsolutePath).mkString(java.io.File.pathSeparator)
  name -> (Seq("-classpath", classPath) ++ (project / Compile / scalacOptions).value ++ allSources)
}

def benchmarkConfigs = Def.task {
  // Small benchmarks: single .scala files
  val smallDir = (benchSmall / Compile / scalaSource).value
  val smallClassPath = (benchSmall / Compile / dependencyClasspath).value
    .map(_.data.getAbsolutePath)
    .mkString(java.io.File.pathSeparator)

  val smallScalacOptions = (benchSmall / Compile / scalacOptions).value
  val smallEntries = smallDir.listFiles.toSeq.flatMap { entry =>
    if (entry.isFile && entry.getName.endsWith(".scala")) {
      val name = entry.getName.stripSuffix(".scala")
      Some(name -> (Seq("-classpath", smallClassPath) ++ smallScalacOptions ++ Seq(entry.getAbsolutePath)))
    } else None
  }

  // Big benchmarks: each has its own subproject
  val bigEntries = Seq(
    bigBenchmarkConfig(benchCaskApp).value,
    bigBenchmarkConfig(benchDottyUtil).value,
    bigBenchmarkConfig(benchFansi, includeTests = true).value,
    bigBenchmarkConfig(benchIndigo).value, // Requires Scala 3.6.4+
    bigBenchmarkConfig(benchRe2s).value,
    bigBenchmarkConfig(benchScalaParallelCollections).value,
    bigBenchmarkConfig(benchScalaParserCombinators, includeTests = true).value,
    bigBenchmarkConfig(benchScalaToday).value,
    bigBenchmarkConfig(benchScalaYaml, includeTests = true).value,
    bigBenchmarkConfig(benchScalaz).value,
    bigBenchmarkConfig(benchSourcecode, includeTests = true).value,
    // bigBenchmarkConfig(benchStdlib213).value,
    bigBenchmarkConfig(benchTastyQuery).value,
    bigBenchmarkConfig(benchTictactoe, includeTests = true).value,
  )

  (smallEntries ++ bigEntries).toMap
}
