package bench

import org.openjdk.jmh.annotations.Benchmark

class CompilationBenchmarksSmall extends CompilationBenchmarks:
  @Benchmark def exhaustivityI = scalac(Config.exhaustivityI, expectedSources = 1)
  @Benchmark def exhaustivityS = scalac(Config.exhaustivityS, expectedSources = 1)
  @Benchmark def exhaustivityT = scalac(Config.exhaustivityT, expectedSources = 1)
  @Benchmark def exhaustivityV = scalac(Config.exhaustivityV, expectedSources = 1)
  @Benchmark def findRef = scalac(Config.findRef, expectedSources = 1)
  @Benchmark def helloWorld = scalac(Config.helloWorld, expectedSources = 1)
  @Benchmark def i1535 = scalac(Config.i1535, expectedSources = 1)
  @Benchmark def i1687 = scalac(Config.i1687, expectedSources = 1)
  @Benchmark def implicitCache = scalac(Config.implicitCache, expectedSources = 1)
  @Benchmark def implicitInductive = scalac(Config.implicitInductive, expectedSources = 1)
  @Benchmark def implicitNums = scalac(Config.implicitNums, expectedSources = 1)
  @Benchmark def implicitScopeLoop = scalac(Config.implicitScopeLoop, expectedSources = 1)
  @Benchmark def matchTypeBubbleSort = scalac(Config.matchTypeBubbleSort, expectedSources = 1)
  @Benchmark def patmatexhaust = scalac(Config.patmatexhaust, expectedSources = 1)
  @Benchmark def tuple = scalac(Config.tuple, expectedSources = 1)
  @Benchmark def tuple22Apply = scalac(Config.tuple22Apply, expectedSources = 1)
  @Benchmark def tuple22Cons = scalac(Config.tuple22Cons, expectedSources = 1)
  @Benchmark def tuple22Creation = scalac(Config.tuple22Creation, expectedSources = 1)
  @Benchmark def tuple22Size = scalac(Config.tuple22Size, expectedSources = 1)
  @Benchmark def tuple22Tails = scalac(Config.tuple22Tails, expectedSources = 1)
