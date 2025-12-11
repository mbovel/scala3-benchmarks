package bench

import bench.compilers.{DottyCompiler, XsbtiCompiler}
import org.openjdk.jmh.annotations.Benchmark

class CompilationBenchmarksSmall extends CompilationBenchmarks:
  @Benchmark def exhaustivityI =
    assert(Config.exhaustivityI.sources.size == 1)
    DottyCompiler.compile(Config.exhaustivityI.sources, Config.exhaustivityI.options, outDir)

  @Benchmark def exhaustivityS =
    assert(Config.exhaustivityS.sources.size == 1)
    DottyCompiler.compile(Config.exhaustivityS.sources, Config.exhaustivityS.options, outDir)

  @Benchmark def exhaustivityT =
    assert(Config.exhaustivityT.sources.size == 1)
    DottyCompiler.compile(Config.exhaustivityT.sources, Config.exhaustivityT.options, outDir)

  @Benchmark def exhaustivityV =
    assert(Config.exhaustivityV.sources.size == 1)
    DottyCompiler.compile(Config.exhaustivityV.sources, Config.exhaustivityV.options, outDir)

  @Benchmark def findRef =
    assert(Config.findRef.sources.size == 1)
    DottyCompiler.compile(Config.findRef.sources, Config.findRef.options, outDir)

  @Benchmark def helloWorld =
    assert(Config.helloWorld.sources.size == 1)
    DottyCompiler.compile(Config.helloWorld.sources, Config.helloWorld.options, outDir)

  @Benchmark def i1535 =
    assert(Config.i1535.sources.size == 1)
    DottyCompiler.compile(Config.i1535.sources, Config.i1535.options, outDir)

  @Benchmark def i1687 =
    assert(Config.i1687.sources.size == 1)
    DottyCompiler.compile(Config.i1687.sources, Config.i1687.options, outDir)

  @Benchmark def implicitCache =
    assert(Config.implicitCache.sources.size == 1)
    DottyCompiler.compile(Config.implicitCache.sources, Config.implicitCache.options, outDir)

  @Benchmark def implicitInductive =
    assert(Config.implicitInductive.sources.size == 1)
    DottyCompiler.compile(Config.implicitInductive.sources, Config.implicitInductive.options, outDir)

  @Benchmark def implicitNums =
    assert(Config.implicitNums.sources.size == 1)
    DottyCompiler.compile(Config.implicitNums.sources, Config.implicitNums.options, outDir)

  @Benchmark def implicitScopeLoop =
    assert(Config.implicitScopeLoop.sources.size == 1)
    DottyCompiler.compile(Config.implicitScopeLoop.sources, Config.implicitScopeLoop.options, outDir)

  @Benchmark def matchTypeBubbleSort =
    assert(Config.matchTypeBubbleSort.sources.size == 1)
    DottyCompiler.compile(Config.matchTypeBubbleSort.sources, Config.matchTypeBubbleSort.options, outDir)

  @Benchmark def patmatexhaust =
    assert(Config.patmatexhaust.sources.size == 1)
    DottyCompiler.compile(Config.patmatexhaust.sources, Config.patmatexhaust.options, outDir)

  @Benchmark def tuple =
    assert(Config.tuple.sources.size == 1)
    DottyCompiler.compile(Config.tuple.sources, Config.tuple.options, outDir)

  @Benchmark def tuple22Apply =
    assert(Config.tuple22Apply.sources.size == 1)
    DottyCompiler.compile(Config.tuple22Apply.sources, Config.tuple22Apply.options, outDir)

  @Benchmark def tuple22Cons =
    assert(Config.tuple22Cons.sources.size == 1)
    DottyCompiler.compile(Config.tuple22Cons.sources, Config.tuple22Cons.options, outDir)

  @Benchmark def tuple22Creation =
    assert(Config.tuple22Creation.sources.size == 1)
    DottyCompiler.compile(Config.tuple22Creation.sources, Config.tuple22Creation.options, outDir)

  @Benchmark def tuple22Size =
    assert(Config.tuple22Size.sources.size == 1)
    DottyCompiler.compile(Config.tuple22Size.sources, Config.tuple22Size.options, outDir)

  @Benchmark def tuple22Tails =
    assert(Config.tuple22Tails.sources.size == 1)
    DottyCompiler.compile(Config.tuple22Tails.sources, Config.tuple22Tails.options, outDir)

  @Benchmark def xsbtiHelloWorld =
    assert(Config.helloWorld.sources.size == 1)
    XsbtiCompiler.compile(Config.helloWorld.sources, Config.helloWorld.options, outDir)
